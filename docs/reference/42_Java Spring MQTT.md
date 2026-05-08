---
title: Java Spring MQTT
created: 2026-05-08 22:29:19
category: Java-Spring
tags:
  - Java
  - Spring
  - MQTT
  - IoT
  - 消息协议
  - 发布订阅
---

<!-- markdownlint-disable MD025 -->

# Java Spring MQTT

## 为什么要学 MQTT

上一节我们学了 WebSocket — 浏览器和服务端之间的全双工实时通信。但如果你面对的不是浏览器，而是成千上万个 IoT 设备（传感器、智能路灯、工业仪表），WebSocket 就显得太重了：WebSocket 连接需要维护 TCP 长连接，每个设备都要和服务端直接握手，对于内存和带宽有限的嵌入式设备来说开销太大。

MQTT（Message Queuing Telemetry Transport）正是为这类场景设计的：极度轻量、低带宽占用、支持不可靠网络环境下的通信。它是 IoT 领域事实上的标准协议，理解它对于涉及设备通信的 Spring 应用是必要的。

## 核心概念

### MQTT 是什么

MQTT 是一种基于发布/订阅模式的轻量级消息传输协议，运行在 TCP/IP 之上。它的设计目标是极低的网络带宽占用和最小的设备资源消耗，适合在低带宽、不可靠网络环境下连接大量远程设备。

**换个说法：** WebSocket 像打电话 — 点对点、独占线路、双方都"在线"。MQTT 像订阅杂志 — 你告诉邮局"我订了《传感器月刊》"，每当新一期出版，邮局自动送到你家。你不需要和出版社保持连接，万一你不在家（设备离线），邮局还会根据你的"遗嘱"决定怎么处理。

### 为什么需要 MQTT

**痛点场景：** 一个智慧农业系统需要采集 5000 个田间传感器每小时上报的温湿度数据。这些传感器使用电池供电、4G 网络连接。如果用 HTTP：每个传感器每小时发一个 POST 请求，5000 个设备每小时 5000 次连接 + 断开，HTTP 头部开销大，对于按流量计费的 SIM 卡是浪费。如果用 WebSocket：维护 5000 个长连接对服务端压力大，而且田间网络不稳定，连接频繁断开重建。

**设计动机：** MQTT 一次连接可以持续复用，消息头部最小只有 2 字节。支持 QoS（服务质量）分级，确保消息在有丢包的网络环境下仍能可靠送达。支持遗嘱消息（设备异常断线时自动发送指定消息）。

### 没有 MQTT 会怎样

**困境：** 用 HTTP 轮询上报数据 — 设备功耗高（每次建立连接），网络开销大，服务端要处理海量短连接。用 WebSocket — 长连接维护成本高，对低功耗设备不友好。

**有了 MQTT 之后：** 设备通过 MQTT Broker 桥接，不需要直连业务服务端。Broker 负责连接管理、消息路由、持久化。设备只需维持一条到 Broker 的轻量连接，功耗和带宽消耗降到最低。

## 概念深入解释

### MQTT 架构

```
┌─────────┐  publish  ┌──────────────┐  subscribe  ┌─────────┐
│ 传感器 A │ ────────→ │              │ ←────────── │ 业务服务 │
└─────────┘           │  MQTT Broker │             └─────────┘
                      │  (如 EMQX)   │
┌─────────┐  publish  │              │  subscribe  ┌─────────┐
│ 传感器 B │ ────────→ │              │ ←────────── │ 监控大屏 │
└─────────┘           └──────────────┘             └─────────┘
```

**核心角色：**

| 角色 | 职责 |
|------|------|
| Publisher（发布者） | 向特定 Topic 发布消息 |
| Subscriber（订阅者） | 订阅特定 Topic，接收消息 |
| Broker（代理） | 接收所有消息，根据 Topic 路由给订阅者 |
| Topic（主题） | 消息的分类标签，如 `farm/sensor/temperature` |

### MQTT 核心特性

**QoS（服务质量）三级：**

| QoS | 含义 | 消息可靠性 | 适用场景 |
|-----|------|-----------|----------|
| 0 | 最多一次（At most once） | 可能丢失 | 传感器周期性上报（丢一次无所谓） |
| 1 | 至少一次（At least once） | 可能重复 | 重要数据，允许去重 |
| 2 | 恰好一次（Exactly once） | 不丢不重 | 关键指令，如阀门开关 |

**遗嘱消息（Last Will）：** 设备连接时预设一条"遗嘱"，如果设备异常断线（非正常 DISCONNECT），Broker 自动将遗嘱发布给订阅者。比如传感器离线时自动通知："传感器 #42 已离线"。

**保留消息（Retained Message）：** 发布消息时标记 `retain=true`，Broker 保留该 Topic 的最后一条消息。新订阅者连接时立即收到这条消息，不需要等待下一次发布。

**会话保持（Persistent Session）：** 设备断线重连后，Broker 能记住其订阅信息和离线期间的消息（对 QoS 1/2），重连后自动推送。

### Spring 集成 MQTT

Spring 没有自己的 MQTT 库，但可以通过 Spring Integration 或直接使用 Eclipse Paho 客户端集成。

**方式一：Spring Integration MQTT**

```java
@Configuration
public class MqttConfig {

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
            new MqttPahoMessageDrivenChannelAdapter(
                "tcp://broker:1883",
                "spring-client-id",
                "farm/sensor/#",           // 订阅的 Topic（通配符）
                "farm/control/#");
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMqttMessage(Message<?> message) {
        System.out.println("Received: " + message.getPayload());
    }
}
```

**方式二：直接使用 Eclipse Paho 客户端**

适合更灵活的控制场景，在 Service 层封装 MQTT 客户端操作：

```java
@Service
public class MqttService {

    private final MqttClient client;

    public MqttService() throws MqttException {
        client = new MqttClient("tcp://broker:1883", "service-client-id");
        client.connect();
        client.subscribe("farm/sensor/#", (topic, message) -> {
            processSensorData(topic, message.getPayload());
        });
    }

    public void publishCommand(String deviceId, String command) {
        client.publish("farm/control/" + deviceId,
            new MqttMessage(command.getBytes()));
    }
}
```

### Topic 设计规范

MQTT Topic 是分层级的字符串，用 `/` 分隔：

```
farm/sensor/temperature         # 农场传感器温度
farm/sensor/humidity            # 农场传感器湿度
farm/control/pump/{deviceId}    # 水泵控制指令
```

**通配符：**

| 通配符 | 含义 | 示例 |
|--------|------|------|
| `+` | 匹配单层 | `farm/sensor/+` 匹配 temperature、humidity |
| `#` | 匹配多层（必须放在最后） | `farm/#` 匹配所有 farm 下的 Topic |

### MQTT Broker 选型

| Broker | 特点 | 适用场景 |
|--------|------|----------|
| EMQX | 高性能、Web Dashboard、规则引擎、国内生态好 | 大规模 IoT 平台，中文社区支持 |
| Mosquitto | 轻量、C 语言实现、资源占用极低 | 嵌入式网关、小规模部署 |
| HiveMQ | 企业版功能丰富、Kafka 集成 | 企业级 IoT 平台 |
| VerneMQ | Erlang 实现、集群支持好 | 需要高可用集群 |

## 核心要点

1. **MQTT 是为 IoT 设计的轻量级协议：** 低带宽、低功耗、支持不稳定网络，不适合浏览器直连。
2. **发布/订阅模式解耦设备和服务：** 设备只需要知道 Broker 地址，不需要知道谁是消费者。
3. **QoS 按场景选择：** 周期性传感器数据用 QoS 0，重要告警用 QoS 1，关键控制指令用 QoS 2。
4. **Topic 设计需要规范：** 分层命名、通配符合理使用，避免 Topic 爆炸。
5. **Spring 集成首选 Spring Integration MQTT：** 与 Spring 的 Message Channel 模型统一，或用 Paho 客户端获得更灵活的控制。

## 常见误区

- **把 MQTT 用于浏览器实时通信。** MQTT 不是为浏览器设计的。虽然 EMQX 支持 MQTT over WebSocket，但浏览器端更适合用 WebSocket + STOMP 或直接 Socket.IO。MQTT 的优势在 IoT 低功耗设备场景，浏览器场景下它的优势体现不出来。
- **所有消息都用 QoS 2。** QoS 2 有四次握手，延迟和开销最高。大多数传感器数据上报用 QoS 0 或 1 就够了。只在关键指令（如远程关机、阀门控制）场景用 QoS 2。
- **Topic 设计成扁平的（无层级）。** 使用 `sensor_temperature`、`sensor_humidity` 而非 `sensor/temperature`、`sensor/humidity`，失去了通配符订阅的优势。无法用 `sensor/#` 一次订阅所有传感器数据。
- **在 Broker 端做复杂业务逻辑。** EMQX 的规则引擎适合做简单的数据转发和格式转换，不适合做复杂业务处理。业务逻辑应该在 Spring 应用中处理，Broker 只做消息路由。
- **忘记处理 MQTT 客户端的断线重连。** Paho 客户端默认有自动重连机制，但需要正确配置 `setAutomaticReconnect(true)` 和 `setCleanSession(false)`（保持会话），否则断线期间的消息会丢失。
- **MQTT 连接数超过 Broker 限制。** 每个 Broker 都有最大连接数限制，大规模部署时需要做 Broker 集群。了解你使用的 Broker 的并发能力和配置参数。

## 与其他概念的关联

- **前置：** [Java Spring Service 层](./30_Java%20Spring%20Service%20层.md) -- MQTT 消息处理逻辑封装在 Service 层
- **前置：** [Java Spring 异步与定时任务](./32_Java%20Spring%20异步与定时任务.md) -- MQTT 消息到达后通常异步处理
- **并行：** [Java Spring WebSocket](./41_Java%20Spring%20WebSocket.md) -- WebSocket 用于浏览器实时通信，MQTT 用于设备实时通信
- **并行：** [Java Spring Modbus](./43_Java%20Spring%20Modbus.md) -- Modbus 是工业设备直连协议，MQTT 是设备联网后的消息协议
- **并行：** [Java Spring 通信协议选型](./44_Java%20Spring%20通信协议选型.md) -- 不同场景下通信协议的选型决策
- **后续：** [Java Spring Cloud 消息队列](../Spring_Cloud/Java Spring Cloud 消息队列.md) -- MQTT Broker 的消息可以桥接到 Kafka/RabbitMQ 进入后端微服务体系
