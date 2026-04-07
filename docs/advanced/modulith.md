# Spring Modulith 模块化单体 / Modular Monolith

> 微服务不是银弹。Spring Modulith 提供了一种在单体应用中实现模块化的方案，兼具单体的简单性和微服务的边界清晰性。

## 1. 概述 / Overview

Spring Modulith 是 Spring 官方的模块化单体框架。它帮助开发者在单个 Spring Boot 应用中定义清晰的模块边界，通过事件驱动实现模块间松耦合，并提供模块边界验证和文档生成能力。当业务复杂度不足以支撑微服务的运维成本时，模块化单体是更务实的选择。

## 2. 核心概念 / Core Concepts

### 2.1 架构选型光谱

```
单体 (Monolith)          模块化单体 (Modular Monolith)          微服务 (Microservices)
    │                              │                                  │
    │  所有代码混在一起              │  模块边界清晰                     │  独立部署的服务
    │  简单但容易腐化               │  单体部署 + 模块隔离              │  复杂但独立演进
    │                              │  可渐进式拆分为微服务              │
    │◄─────── 复杂度低 ──────────────────────────── 复杂度高 ────────▶│
    │◄─────── 运维简单 ──────────────────────────── 运维复杂 ────────▶│
```

### 2.2 模块结构

Spring Modulith 将主包下的每个直接子包视为一个应用模块（Application Module）：

```
com.example.app/                    ← 主包
├── order/                          ← order 模块
│   ├── Order.java                  ← 公开 API（包级别 public）
│   ├── OrderService.java           ← 公开 API
│   └── internal/                   ← 模块内部（其他模块不可访问）
│       ├── OrderRepository.java
│       └── OrderValidator.java
│
├── inventory/                      ← inventory 模块
│   ├── InventoryService.java       ← 公开 API
│   └── internal/
│       └── InventoryRepository.java
│
├── user/                           ← user 模块
│   ├── UserService.java
│   └── internal/
│       └── UserRepository.java
│
└── AppApplication.java             ← @SpringBootApplication
```

**模块可见性规则：**

| 位置 | 可见性 |
|------|--------|
| 模块根包（如 `order/`） | 公开 API，其他模块可访问 |
| 模块 `internal/` 子包 | 模块内部，其他模块不可访问 |
| 其他子包 | 默认内部，可通过 `@NamedInterface` 公开 |

### 2.3 模块间通信

| 方式 | 耦合度 | 说明 |
|------|--------|------|
| 直接调用公开 API | 高 | 简单但形成编译期依赖 |
| Spring Application Events | 低 | 异步解耦，推荐方式 |
| Event Externalization | 最低 | 事件发布到 MQ，为拆分微服务做准备 |

**事件驱动模块通信：**

```
Order 模块                              Inventory 模块
    │                                        │
    │  创建订单                                │
    │  发布 OrderCreated 事件                  │
    │──── ApplicationEventPublisher ────────▶│
    │                                        │  @ApplicationModuleListener
    │                                        │  扣减库存
    │                                        │
    │  事件存储在 Event Publication Log        │
    │  确保至少一次投递                         │
```

### 2.4 核心注解和 API

| 注解/类 | 作用 |
|---------|------|
| `@ApplicationModule` | 自定义模块配置（允许的依赖等） |
| `@ApplicationModuleListener` | 异步事件监听（自动事务 + 完成确认） |
| `@NamedInterface` | 声明模块的命名接口（公开特定子包） |
| `ApplicationModules` | 模块结构分析入口 |
| `ApplicationModuleTest` | 模块隔离测试 |
| `Documenter` | 生成模块文档（PlantUML / Asciidoc） |

### 2.5 Event Publication Log

Spring Modulith 提供事件发布日志，确保事件的可靠投递：

```
┌─────────────────────────────────────────────────┐
│           Event Publication Log                  │
│                                                  │
│  ID | Event Type      | Published | Completed   │
│  1  | OrderCreated    | 10:00:01  | 10:00:02    │
│  2  | OrderCreated    | 10:00:05  | NULL ← 未完成│
│                                                  │
│  应用重启后自动重新投递未完成的事件                  │
└─────────────────────────────────────────────────┘
```

支持的存储：JDBC（JPA）、MongoDB、Neo4j

### 2.6 渐进式拆分路径

```
阶段 1: 模块化单体
    │  模块边界清晰，事件驱动通信
    │
    ▼
阶段 2: Event Externalization
    │  事件发布到 Kafka/RabbitMQ
    │  其他服务可以消费
    │
    ▼
阶段 3: 拆分微服务
    │  将模块提取为独立服务
    │  事件通信已就绪，改动最小
```

## 3. 快速集成 / Quick Start

### 3.1 Maven 依赖

| 依赖 | 说明 |
|------|------|
| `spring-modulith-starter-core` | 核心模块（运行时支持） |
| `spring-modulith-starter-jpa` | JPA Event Publication Log |
| `spring-modulith-starter-jdbc` | JDBC Event Publication Log |
| `spring-modulith-starter-mongodb` | MongoDB Event Publication Log |
| `spring-modulith-starter-test` | 模块测试支持 |
| `spring-modulith-docs` | 文档生成 |
| `spring-modulith-events-kafka` | 事件外部化到 Kafka |
| `spring-modulith-events-amqp` | 事件外部化到 RabbitMQ |

### 3.2 关键配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.modulith.events.republish-outstanding-events-on-restart` | `false` | 重启时重新投递未完成事件 |
| `spring.modulith.events.jdbc.schema-initialization.enabled` | `false` | 自动创建事件表 |

## 4. 进阶要点 / Advanced Topics

- **模块依赖验证** — `ApplicationModules.of(App.class).verify()` 检测非法跨模块依赖
- **模块文档生成** — `Documenter` 生成 PlantUML 组件图和 Asciidoc 文档
- **@ApplicationModuleTest** — 只加载目标模块及其依赖，加速集成测试
- **Scenario API** — 测试事件驱动流程：发布事件 → 验证监听器执行 → 验证结果
- **Event Externalization** — `@Externalized` 注解将事件发布到 Kafka/RabbitMQ
- **Observability** — 自动为模块间事件添加 Micrometer 观测指标
- **与 ArchUnit 对比** — Spring Modulith 提供运行时支持（事件、事务），ArchUnit 只做静态架构验证

## 5. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 模块验证失败 | 模块间存在非法依赖 | 通过事件解耦或调整模块结构 |
| 事件监听器不执行 | 未使用 `@ApplicationModuleListener` | 使用正确的注解 |
| 事件重复消费 | 监听器未幂等 | 确保监听器幂等性 |
| 循环依赖 | 模块 A 依赖 B，B 又依赖 A | 提取公共模块或用事件解耦 |
| 事件丢失 | 未配置 Event Publication Log | 引入 `spring-modulith-starter-jpa` |

## 6. 示例项目 / Example

> 示例项目位于 [`examples/spring-modulith-demo/`](../../examples/spring-modulith-demo/)（待创建）
>
> 将演示：模块化结构、事件驱动通信、模块验证测试、Event Publication Log、文档生成

## 7. 参考链接 / References

- [Spring Modulith Reference](https://docs.spring.io/spring-modulith/reference/)
- [Spring Blog — Spring Modulith](https://spring.io/blog/2022/10/21/introducing-spring-modulith)
- [Baeldung — Spring Modulith](https://www.baeldung.com/spring-modulith)
