# 微服务模块 (microservice)

> Spring Cloud 微服务架构：网关、注册发现、配置中心、服务通信、熔断降级、分布式事务、消息队列、可观测性

## 设计决策

### 为什么需要这个模块

单体应用向微服务转型是 Java 后端开发的必经之路。Spring Cloud 提供了完整的微服务解决方案，但组件众多、配置复杂。本模块帮助开发者系统化地理解和实践微服务架构。

### 为什么这么设计

- **选择了**：按微服务架构层次（网关 → 服务治理 → 通信 → 容错 → 事务 → 观测）自顶向下组织
- **而不是**：按 Spring Cloud 组件列表组织（如按 Netﬂix/Alibaba 等厂商分类）
- **原因**：架构层次更有助于构建完整的认知模型，而非仅仅是罗列组件

## 关键类型与接口

### 子主题与技术栈

| 子主题 | 文档 | 核心组件 |
|--------|------|----------|
| API 网关 | gateway.md | Spring Cloud Gateway |
| 服务发现 | service-discovery.md | Nacos, Consul |
| 配置中心 | config-center.md | Nacos Config, Spring Cloud Config |
| 服务通信 | service-communication.md | OpenFeign, gRPC |
| 熔断降级 | circuit-breaker.md | Resilience4j, Sentinel |
| 分布式事务 | distributed-transaction.md | Seata |
| 消息队列 | message-queue.md | RabbitMQ, Kafka |
| 可观测性 | observability.md | OpenTelemetry, Micrometer |

### 微服务综合示例项目

`examples/spring-microservice-demo/` 为多模块 Maven 项目：

```text
spring-microservice-demo/
├── gateway-service/     # API 网关
├── user-service/        # 用户服务
├── order-service/       # 订单服务
└── common/              # 公共模块(DTO, 工具类)
```

## 模块结构

```text
docs/reference/microservice/
├── README.md                  # 微服务篇索引
├── gateway.md                 # API 网关
├── service-discovery.md       # 服务注册与发现
├── config-center.md           # 配置中心
├── service-communication.md   # 服务间通信
├── circuit-breaker.md         # 熔断降级
├── distributed-transaction.md # 分布式事务
├── message-queue.md           # 消息队列
└── observability.md           # 可观测性
```

## 与其他模块的关系

### 依赖

- **核心基础模块**：每个微服务本身是 Spring Boot 应用
- **数据库模块**：各微服务独立选择数据库
- **框架核心模块**：Security（认证授权）、Actuator（健康检查）

### 被依赖

- **进阶主题模块**：容器化部署

### 依赖关系图

```text
微服务 (microservice)
  ↑ 基于
  ├── 核心基础 (core)        → 每个微服务是独立的 Spring Boot 应用
  ├── 数据库 (database)       → 各服务独立数据存储
  └── 框架核心 (framework)    → 安全认证、健康检查、日志
```

## 详细技术参考

以下为各子主题的完整技术参考文档，包含核心概念、配置示例、代码片段、进阶要点和常见问题：

| 子主题 | 参考文档 |
|--------|----------|
| API 网关 | [reference/microservice/gateway.md](../reference/microservice/gateway.md) |
| 服务注册与发现 | [reference/microservice/service-discovery.md](../reference/microservice/service-discovery.md) |
| 配置中心 | [reference/microservice/config-center.md](../reference/microservice/config-center.md) |
| 服务间通信 | [reference/microservice/service-communication.md](../reference/microservice/service-communication.md) |
| 熔断降级 | [reference/microservice/circuit-breaker.md](../reference/microservice/circuit-breaker.md) |
| 分布式事务 | [reference/microservice/distributed-transaction.md](../reference/microservice/distributed-transaction.md) |
| 消息队列 | [reference/microservice/message-queue.md](../reference/microservice/message-queue.md) |
| 可观测性 | [reference/microservice/observability.md](../reference/microservice/observability.md) |

## 注意事项

- 微服务示例应包含服务间调用的完整链路（gateway → user-service → order-service）
- Nacos vs Consul 是常见的注册中心选型问题，文档需客观对比
- 分布式事务（Seata）的 AT/TCC/Saga 模式适用场景不同，需要逐一说明
- 可观测性（Tracing + Metrics + Logging）三支柱需要给出完整的集成方案
- 本地开发时多服务端口冲突是常见问题，需在文档中给出端口规划建议
