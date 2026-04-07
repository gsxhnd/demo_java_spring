# Service Communication — 服务间通信 / Inter-Service Communication

> OpenFeign 声明式 HTTP 调用 / gRPC 高性能 RPC 通信

## 1. 概述 / Overview

微服务之间需要相互调用。两种主流方案：

| 方案 | 协议 | 风格 | 适用场景 |
|---|---|---|---|
| **OpenFeign** | HTTP/1.1 (REST) | 声明式接口 | 通用业务调用，简单易用 |
| **gRPC** | HTTP/2 (Protobuf) | IDL 定义 | 高性能、低延迟、跨语言 |

### OpenFeign vs RestTemplate vs WebClient vs gRPC

| 特性 | OpenFeign | RestTemplate | WebClient | gRPC |
|---|---|---|---|---|
| 编程模型 | 声明式（接口 + 注解） | 命令式 | 响应式 | IDL + 代码生成 |
| 协议 | HTTP/1.1 | HTTP/1.1 | HTTP/1.1 | HTTP/2 |
| 序列化 | JSON | JSON | JSON | Protobuf（二进制） |
| 性能 | 中 | 中 | 中高 | 高 |
| 服务发现集成 | 原生支持 | 需手动 | 需手动 | 需额外集成 |
| 负载均衡 | 内置 | 需配置 | 需配置 | 需额外集成 |
| 学习成本 | 低 | 低 | 中 | 高 |
| 跨语言 | 否（Java） | 否 | 否 | 是（多语言） |

---

## 2. 核心概念 / Core Concepts

### OpenFeign 工作原理

```
┌─────────────────────────────────────────────┐
│ User Service                                 │
│                                              │
│  @FeignClient("order-service")               │
│  interface OrderClient {                     │
│      @GetMapping("/api/orders/{id}")         │
│      Order getOrder(@PathVariable Long id);  │
│  }                                           │
│                                              │
│  调用 orderClient.getOrder(1)                │
│       │                                      │
│       ▼                                      │
│  Feign 动态代理                               │
│       │                                      │
│       ▼                                      │
│  Spring Cloud LoadBalancer                   │
│  (从注册中心获取 order-service 实例列表)        │
│       │                                      │
│       ▼                                      │
│  HTTP 请求: GET http://10.0.1.5:8082/api/... │
└──────────────────────────────────────────────┘
```

### gRPC 工作原理

```
┌──────────────┐                    ┌──────────────┐
│ gRPC Client  │  HTTP/2 + Protobuf │ gRPC Server  │
│ (User Svc)   │ ──────────────────→│ (Order Svc)  │
│              │                    │              │
│ Stub (生成)  │  双向流式通信        │ Service Impl │
└──────────────┘                    └──────────────┘
        ▲                                   ▲
        │                                   │
        └──────── .proto 文件 ──────────────┘
                  (共享接口定义)
```

### gRPC 通信模式

| 模式 | 说明 | 适用场景 |
|---|---|---|
| Unary | 一请求一响应（类似 REST） | 普通查询 |
| Server Streaming | 一请求多响应 | 数据推送、日志流 |
| Client Streaming | 多请求一响应 | 文件上传、批量提交 |
| Bidirectional Streaming | 双向流 | 实时聊天、协同编辑 |

---

## 3. 快速集成 / Quick Start

### OpenFeign

- 依赖：`spring-cloud-starter-openfeign`、`spring-cloud-starter-loadbalancer`
- 启用：主类加 `@EnableFeignClients`
- 关键配置：

| 配置 | 说明 |
|---|---|
| `spring.cloud.openfeign.client.config.default.connect-timeout` | 连接超时 |
| `spring.cloud.openfeign.client.config.default.read-timeout` | 读取超时 |
| `spring.cloud.openfeign.client.config.default.logger-level` | 日志级别（NONE/BASIC/HEADERS/FULL） |
| `spring.cloud.openfeign.compression.request.enabled` | 请求压缩 |
| `spring.cloud.openfeign.circuitbreaker.enabled` | 启用熔断 |

### gRPC (grpc-spring-boot-starter)

- 依赖：`net.devh:grpc-spring-boot-starter`（社区维护）
- Server 配置：`grpc.server.port`
- Client 配置：`grpc.client.<name>.address`（`discovery:///service-name` 配合服务发现）

---

## 4. 进阶要点 / Advanced Topics

- **Feign 拦截器 (RequestInterceptor)**：统一传递 Token、Trace ID 等 Header
- **Feign 错误解码器 (ErrorDecoder)**：自定义异常处理，将 HTTP 错误码映射为业务异常
- **Feign 日志**：`Logger.Level.FULL` 打印完整请求/响应（调试用）
- **Feign + Resilience4j**：`spring.cloud.openfeign.circuitbreaker.enabled=true`，Feign 接口自动熔断
- **Feign Fallback**：`@FeignClient(fallback = XxxFallback.class)` 降级处理
- **gRPC 拦截器 (Interceptor)**：类似 Feign 拦截器，用于鉴权、日志、Trace
- **gRPC + Protobuf**：`.proto` 文件定义接口，`protobuf-maven-plugin` 自动生成 Java 代码
- **gRPC 健康检查**：`grpc.health.v1.Health` 标准健康检查协议
- **gRPC 负载均衡**：客户端负载均衡（`round_robin`）或通过服务网格（Istio）

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| Feign 调用超时 | 调大 `connect-timeout` 和 `read-timeout` |
| Feign 找不到服务 | 确认注册中心中服务名一致，LoadBalancer 依赖已引入 |
| Feign 传递 Header 丢失 | 实现 `RequestInterceptor` 手动传递 |
| gRPC proto 编译失败 | 检查 `protobuf-maven-plugin` 配置和 protoc 版本 |
| gRPC 序列化性能 | Protobuf 比 JSON 快 5-10 倍，体积小 3-5 倍 |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-microservice-demo/`](../../examples/spring-microservice-demo/)（待生成）

## 7. 参考链接 / References

- [Spring Cloud OpenFeign 官方文档](https://docs.spring.io/spring-cloud-openfeign/reference/)
- [gRPC 官方文档](https://grpc.io/docs/)
- [grpc-spring-boot-starter](https://github.com/yidongnan/grpc-spring-boot-starter)
