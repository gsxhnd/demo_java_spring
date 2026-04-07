# 微服务篇 / Microservice Architecture

> Spring Cloud 微服务生态完整指南

## 单体架构 vs 微服务架构 / Monolith vs Microservice

| 维度 | 单体架构 (Monolith) | 微服务架构 (Microservice) |
|---|---|---|
| 部署 | 整体打包部署 | 独立部署，互不影响 |
| 扩展 | 整体扩容 | 按服务独立扩容 |
| 技术栈 | 统一 | 每个服务可选不同技术栈 |
| 团队 | 所有人改同一个代码库 | 每个团队负责自己的服务 |
| 复杂度 | 代码复杂度高 | 运维复杂度高 |
| 适用规模 | 小团队、早期项目 | 大团队、复杂业务 |

## Spring Cloud 组件全景图 / Component Overview

```
                        ┌─────────────┐
                        │   Client    │
                        └──────┬──────┘
                               │
                        ┌──────▼──────┐
                        │   Gateway   │  ← Spring Cloud Gateway
                        │  (路由/限流) │
                        └──────┬──────┘
                               │
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
       ┌────────────┐  ┌────────────┐  ┌────────────┐
       │ User Service│  │Order Service│  │Pay Service │
       └──────┬─────┘  └──────┬─────┘  └──────┬─────┘
              │               │                │
              │    ┌──────────▼──────────┐     │
              │    │  OpenFeign / gRPC   │     │  ← 服务间通信
              │    │  (声明式调用)        │     │
              │    └─────────────────────┘     │
              │                                │
       ┌──────▼────────────────────────────────▼──────┐
       │           Service Discovery (Nacos)           │  ← 注册发现
       │           Config Center (Nacos)               │  ← 配置中心
       └──────────────────────────────────────────────┘
              │                                │
       ┌──────▼──────┐                 ┌───────▼─────┐
       │ Circuit     │                 │ Message     │
       │ Breaker     │                 │ Queue       │  ← RabbitMQ / Kafka
       │ (Resilience4j)│               │ (异步通信)   │
       └─────────────┘                 └─────────────┘
              │                                │
       ┌──────▼────────────────────────────────▼──────┐
       │         OpenTelemetry (链路追踪/指标/日志)      │  ← 可观测性
       └──────────────────────────────────────────────┘
              │
       ┌──────▼──────┐
       │   Seata     │  ← 分布式事务
       │ (AT/TCC/Saga)│
       └─────────────┘
```

## 版本兼容矩阵 / Version Compatibility

| 组件 | 版本 | 对应 Spring Boot |
|---|---|---|
| Spring Cloud | 2025.1 (Oakwood) | 4.0.x |
| Spring Cloud Alibaba | 2025.1.0.0 | 4.0.x |
| Nacos | 3.0.x | - |
| Sentinel | 2.0.x | - |
| Seata | 2.0.x | - |

> Spring Cloud Alibaba 2025.1.0.0 要求 Java 21+，全面支持虚拟线程和 Jakarta EE 10。

## 微服务拆分原则 / Service Splitting Principles

- **单一职责**：每个服务只做一件事（用户、订单、支付）
- **高内聚低耦合**：服务内部紧密相关，服务之间松散依赖
- **数据自治**：每个服务拥有自己的数据库，不共享数据库
- **接口契约**：服务间通过 API 契约通信，不依赖内部实现
- **渐进拆分**：从单体开始，按业务边界逐步拆分

---

## 文档列表 / Documents

1. [Spring Cloud Gateway — API 网关](gateway.md)
2. [Service Discovery — 服务注册与发现 (Nacos / Eureka / Consul)](service-discovery.md)
3. [Config Center — 配置中心 (Nacos Config / Spring Cloud Config)](config-center.md)
4. [Service Communication — 服务间通信 (OpenFeign / gRPC)](service-communication.md)
5. [Circuit Breaker — 熔断降级 (Resilience4j / Sentinel)](circuit-breaker.md)
6. [Distributed Transaction — 分布式事务 (Seata)](distributed-transaction.md)
7. [Message Queue — 消息队列 (RabbitMQ / Kafka)](message-queue.md)
8. [Observability — 可观测性 (OpenTelemetry)](observability.md)

## 示例项目 / Example

完整微服务综合示例见 → [`examples/spring-microservice-demo/`](../../examples/spring-microservice-demo/)（待生成）

包含 Gateway + User Service + Order Service 多模块项目，演示完整的微服务调用链。
