---
title: Java Spring Modbus
created: 2026-05-08 22:29:19
category: Java-Spring
tags:
  - Java
  - Spring
  - Modbus
  - 工业协议
  - PLC
  - OT
---

<!-- markdownlint-disable MD025 -->

# Java Spring Modbus

## 为什么要学 Modbus

前面我们学了 WebSocket（浏览器实时通信）和 MQTT（IoT 设备消息协议）。这两种协议都工作在 TCP/IP 网络层之上，设备需要有网络栈和 IP 地址。但在工业自动化和楼宇控制领域，大量设备仍然使用更底层的通信协议 — 它们不一定有 IP 地址，但都有一个 RS-485 串口。Modbus 就是这些设备之间沟通的"普通话"。

如果你的 Spring 应用需要直接读取 PLC（可编程逻辑控制器）的数据、控制变频器转速、采集温控仪表读数，你需要理解 Modbus 协议以及在 Java 中如何集成它。

## 核心概念

### Modbus 是什么

Modbus 是一种应用层消息传输协议，位于 OSI 模型第 7 层。它定义了控制器（Master）如何请求访问其他设备（Slave）、如何响应请求、如何报告错误。自 1979 年发布以来，它已成为工业电子设备之间最常用的连接方式。

**换个说法：** Modbus 就像工业设备的"USB 协议" — 不管设备内部是什么芯片、什么程序，只要它说 Modbus，你就能用同一套指令读取它的数据（比如"把寄存器 #40001 的值给我"）。

### 为什么需要 Modbus

**痛点场景：** 一个工厂有不同厂家出品的 20 种设备 — 西门子 PLC、施耐德变频器、欧姆龙温控器。每种设备都有各自原厂的通信协议和配套软件。你无法用一个统一的系统读取所有设备的数据，只能分别登录各自的监控界面。

**设计动机：** Modbus 是一个开放、免版税、易于部署的协议。绝大多数工业设备都支持 Modbus，它提供了统一的数据访问模型（线圈、寄存器等），不管底层设备是什么品牌。

### 没有 Modbus 会怎样

**困境：** 每种工业设备使用各自的专有协议，集成成本爆炸。一个工厂需要一个设备一个驱动，维护一个庞大的协议适配层。而 Modbus 虽然简单（甚至可以说"古老"），但它的开放性使得跨品牌通信成为可能。

**有了 Modbus 之后：** 任何支持 Modbus 的设备都可以用相同的读取指令访问。你不需要为每种设备写独立的驱动程序，只需要知道它的寄存器地址表。

## 概念深入解释

### Modbus 通信模型

```
┌──────────┐  请求  ┌──────────┐
│  Master  │ ─────→ │  Slave   │
│ (主机)   │ ←───── │ (从机)   │
└──────────┘  响应  └──────────┘
```

Modbus 采用**主从架构**（Master-Slave），通信始终由 Master 发起。Slave 只响应请求，不主动发送数据。

**协议变体：**

| 变体 | 传输层 | 特点 |
|------|--------|------|
| Modbus RTU | 串行（RS-232/RS-485） | 二进制编码、紧凑、工业现场最常用 |
| Modbus ASCII | 串行 | 文本编码、可读性高、速度慢 |
| Modbus TCP | TCP/IP（端口 502） | 以太网传输、适合集成到 IT 系统 |

### 数据模型

Modbus 定义了四种可访问的数据区：

| 类型 | 地址范围 | 读写 | 典型用途 |
|------|----------|------|----------|
| 线圈（Coil） | 00001-09999 | 读写 | 开关量输出（继电器、指示灯） |
| 离散输入（Discrete Input） | 10001-19999 | 只读 | 开关量输入（按钮、限位开关） |
| 保持寄存器（Holding Register） | 40001-49999 | 读写 | 模拟量输出（设定温度、转速） |
| 输入寄存器（Input Register） | 30001-39999 | 只读 | 模拟量输入（当前温度、电压） |

每个地址访问的单位是 bit（线圈/离散输入）或 16-bit word（寄存器）。

### Spring 集成 Modbus

Java 生态中 Modbus 库主要有 `jlibmodbus`、`modbus4j`、`jamod`。以 `jlibmodbus` 为例：

**依赖引入：**

```xml
<dependency>
    <groupId>com.intelligt.modbus</groupId>
    <artifactId>jlibmodbus</artifactId>
    <version>1.2.9.7</version>
</dependency>
```

**Modbus TCP 客户端封装为 Service：**

```java
@Service
public class ModbusService {

    private ModbusMaster master;

    @PostConstruct
    public void init() throws ModbusIOException {
        Modbus.setLogLevel(Modbus.LogLevel.LEVEL_RELEASE);
        master = ModbusMasterFactory.createModbusMasterTCP(
            InetAddress.getByName("192.168.1.100"));
        master.setResponseTimeout(3000);
    }

    // 读取保持寄存器（如读取当前温度值）
    public int[] readHoldingRegisters(int slaveId, int offset, int quantity)
            throws ModbusProtocolException, ModbusIOException {
        if (!master.isConnected()) {
            master.connect();
        }
        return master.readHoldingRegisters(slaveId, offset, quantity);
    }

    // 写入单个寄存器（如设置目标温度）
    public void writeHoldingRegister(int slaveId, int offset, int value)
            throws ModbusProtocolException, ModbusIOException {
        if (!master.isConnected()) {
            master.connect();
        }
        master.writeSingleRegister(slaveId, offset, value);
    }

    @PreDestroy
    public void destroy() throws ModbusIOException {
        if (master != null && master.isConnected()) {
            master.disconnect();
        }
    }
}
```

### 常见集成架构

在实际项目中，Modbus 设备通常通过**边缘网关**或**工业协议转换器**接入 Spring 应用：

```
┌──────────┐  RS-485   ┌──────────────┐  MQTT/HTTP  ┌────────────┐
│ PLC/仪表  │ ←──────→ │ 边缘网关      │ ←─────────→ │ Spring 应用 │
└──────────┘  Modbus   │ (协议转换)    │             └────────────┘
                      └──────────────┘
```

这种架构下，Spring 应用不直接通过 Modbus 协议与设备通信，而是通过网关将 Modbus 数据转为 MQTT 或 HTTP 后接入。直接 Modbus 连接适合设备数量少、本地网络控制的场景。

### 异常处理

Modbus 通信异常需要专门处理：

```java
try {
    int[] data = modbusService.readHoldingRegisters(1, 0, 10);
} catch (ModbusIOException e) {
    // 物理连接问题：设备断电、线缆断开、IP 不可达
    log.error("Modbus connection error", e);
} catch (ModbusProtocolException e) {
    // 协议层问题：非法数据地址、非法数据值
    log.error("Modbus protocol error: {}", e.getExceptionCode());
}
```

`ModbusProtocolException` 携带标准的异常码（Exception Code），帮助定位问题：

| 异常码 | 含义 |
|--------|------|
| 0x01 | 非法功能（设备不支持该操作） |
| 0x02 | 非法数据地址 |
| 0x03 | 非法数据值 |
| 0x04 | 从机设备故障 |

## 核心要点

1. **Modbus 是工业设备的通用协议：** 主从架构，Master 发起请求，Slave 响应。不主动推送数据。
2. **四种数据区各司其职：** 线圈（开关量输出）、离散输入（开关量输入）、保持寄存器（模拟量输出）、输入寄存器（模拟量输入）。
3. **RTU 用于串行现场，TCP 用于以太网集成：** 面向 IT 系统的 Spring 应用通常用 Modbus TCP 或通过网关中转。
4. **Modbus 连接是 Resource：** 需要管理连接生命周期（connect/disconnect），也适合封装为 `@Bean` + `@PreDestroy` 管理。
5. **异常处理要区分连接错误和协议错误：** 两者的恢复策略不同 — 连接错误可重试，协议错误需要排查指令是否正确。

## 常见误区

- **把 Modbus 当作实时通信协议。** Modbus 是轮询协议 — Master 必须主动查询，没有中断或事件推送机制。对于需要实时响应的场景（如紧急停机），Modbus 的轮询延迟可能导致反应不及时。
- **Modbus RTU 和 Modbus TCP 直接混合使用。** RTU 是串行协议（RS-485），TCP 是以太网协议（RJ-45）。两者数据帧格式不同。需要串口转以太网设备（Modbus 网关）来桥接，不能直接混用。
- **在一个 Modbus TCP 连接上并发发送请求导致数据混乱。** Modbus TCP 理论上支持并发，但很多廉价设备实现不规范，并发请求可能导致错误或数据串位。最佳实践是使用专用线程或同步机制串行化对同一设备的请求。
- **忽略字节序（Endianness）问题。** Modbus 规范定义寄存器为 16-bit Big-Endian。但当多个寄存器组合为 32-bit/64-bit 值（如浮点数、长整数）时，不同设备的字节序可能不同。读取后需要根据设备手册做字节序转换。
- **读取大量寄存器时一次性请求超时。** Modbus 协议对单次读取数量有限制（通常 125 个寄存器）。超量请求应分批读取，每次读取一部分，合并结果。
- **长时间运行后 Modbus 连接断开但程序未感知。** TCP 连接可能被防火墙或 NAT 设备静默关闭。应在读取操作中加入"连接检查 + 自动重连"机制，或定期发送心跳。

## 与其他概念的关联

- **前置：** [Java Spring Service 层](./30_Java%20Spring%20Service%20层.md) -- Modbus 通信逻辑封装在 Service 中
- **前置：** [Java Spring 异步与定时任务](./32_Java%20Spring%20异步与定时任务.md) -- Modbus 轮询读取通常用 @Scheduled 定时执行
- **并行：** [Java Spring MQTT](./42_Java%20Spring%20MQTT.md) -- MQTT 和 Modbus 在工业 IoT 中互补：设备层用 Modbus，上云用 MQTT
- **并行：** [Java Spring 通信协议选型](./44_Java%20Spring%20通信协议选型.md) -- 不同场景下通信协议的选型决策
- **后续：** [Java Spring Cloud 消息队列](../Spring_Cloud/Java Spring Cloud 消息队列.md) -- 采集到的 Modbus 数据通过消息队列推入数据处理管道
