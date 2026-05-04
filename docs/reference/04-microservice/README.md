# 微服务篇

> Spring Cloud 微服务生态完整指南：网关、注册发现、配置中心、服务通信、熔断降级、分布式事务、消息队列、可观测性。

## 为什么需要这篇

单体应用向微服务转型是 Java 后端开发的必经之路。Spring Cloud 提供了完整的微服务解决方案，但组件众多、配置复杂。本篇帮助开发者系统化地理解和实践微服务架构。

按微服务架构层次（网关 → 服务治理 → 通信 → 容错 → 事务 → 观测）自顶向下组织，有助于构建完整的认知模型。

## 单体架构 vs 微服务架构

| 维度 | 单体架构 | 微服务架构 |
|---|---|---|
| 部署 | 整体打包部署 | 独立部署，互不影响 |
| 扩展 | 整体扩容 | 按服务独立扩容 |
| 技术栈 | 统一 | 每个服务可选不同技术栈 |
| 团队 | 所有人改同一个代码库 | 每个团队负责自己的服务 |
| 复杂度 | 代码复杂度高 | 运维复杂度高 |
| 适用规模 | 小团队、早期项目 | 大团队、复杂业务 |

## Spring Cloud 组件全景图

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
              │    ┌──────────▼──────────┐     │
              │    │  OpenFeign / gRPC   │     │  ← 服务间通信
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
       │ (Resilience4j)│               └─────────────┘
       └─────────────┘
              │                                │
       ┌──────▼────────────────────────────────▼──────┐
       │         OpenTelemetry (链路追踪/指标/日志)      │  ← 可观测性
       └──────────────────────────────────────────────┘
              │
       ┌──────▼──────┐
       │   Seata     │  ← 分布式事务
       └─────────────┘
```

## 版本兼容矩阵

| 组件 | 版本 | 对应 Spring Boot |
|---|---|---|
| Spring Cloud | 2025.1 (Oakwood) | 4.0.x |
| Spring Cloud Alibaba | 2025.1.0.0 | 4.0.x |
| Nacos | 3.0.x | - |
| Sentinel | 2.0.x | - |
| Seata | 2.0.x | - |

> Spring Cloud Alibaba 2025.1.0.0 要求 Java 21+，全面支持虚拟线程和 Jakarta EE 10。

## 文档列表

1. [Spring Cloud Gateway — API 网关](01-gateway.md)
2. [Service Discovery — 服务注册与发现 (Nacos / Eureka / Consul)](02-service-discovery.md)
3. [Config Center — 配置中心 (Nacos Config / Spring Cloud Config)](03-config-center.md)
4. [Service Communication — 服务间通信 (OpenFeign / gRPC)](04-service-communication.md)
5. [Circuit Breaker — 熔断降级 (Resilience4j / Sentinel)](05-circuit-breaker.md)
6. [Distributed Transaction — 分布式事务 (Seata)](06-distributed-transaction.md)
7. [Message Queue — 消息队列 (RabbitMQ / Kafka)](07-message-queue.md)
8. [Observability — 可观测性 (OpenTelemetry)](08-observability.md)

## 示例项目

综合示例 → `examples/spring-microservice-demo/`（待生成）

```
spring-microservice-demo/
├── gateway-service/     # API 网关
├── user-service/        # 用户服务
├── order-service/       # 订单服务
└── common/              # 公共模块(DTO, 工具类)
```

## 与其他篇章的关系

- **依赖**：[核心基础篇](../01-core/README.md)（每个微服务是独立的 Spring Boot 应用）、[数据库篇](../02-database/README.md)（各服务独立数据存储）、[框架核心篇](../03-framework/README.md)（Security 认证授权、Actuator 健康检查）
- **被依赖**：[进阶主题篇](../05-advanced/README.md)（容器化部署）

## 注意事项

- 微服务示例应包含服务间调用的完整链路（gateway → user-service → order-service）
- Nacos vs Consul 是常见的注册中心选型问题，文档客观对比
- 分布式事务（Seata）的 AT/TCC/Saga 模式适用场景不同，需逐一说明
- 可观测性（Tracing + Metrics + Logging）三支柱需要给出完整的集成方案
- 本地开发时多服务端口冲突是常见问题，文档中给出端口规划建议
