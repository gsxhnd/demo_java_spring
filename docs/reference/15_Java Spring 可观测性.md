---
title: Java Spring 可观测性
created: 2026-05-05 11:27:43
category: Java-Spring
tags:
  - Java
  - Spring
  - Actuator
  - OpenTelemetry
  - Observability
---

<!-- markdownlint-disable MD025 -->

# Java Spring 可观测性

## 为什么要学可观测性

前面几节让项目跑起来了 -- 有了 Spring Boot 概述、自动配置、Starter、项目结构、配置文件。应用能启动、能响应请求。但接下来一个自然的问题是：**它跑得怎么样？**

- 请求响应慢，慢在哪一步？
- 某个接口偶尔报错，什么原因？
- 内存使用在持续增长，是不是有泄漏？
- 下游服务超时了，影响了哪些请求？

这些问题靠"看代码"是回答不了的，你需要可观测性（Observability）。可观测性是生产环境的"眼睛"，让你在不修改代码的情况下理解系统的内部状态。

把可观测性放在 Part 3（跑起来之后）而不是更后面，是因为它应该从项目初期就建立，而不是出了问题再补。

---

## 核心概念

### 可观测性是什么

**可观测性（Observability）是通过系统的外部输出（日志、指标、链路追踪）来推断系统内部状态的能力。它由三大支柱组成：Traces（链路追踪）、Metrics（指标）、Logs（日志）。**

类比：可观测性就像汽车的仪表盘。你不需要打开引擎盖就能知道油量、转速、水温、故障码。三大支柱分别对应：行车记录仪（Traces -- 记录每次行程的完整路径）、仪表盘读数（Metrics -- 实时数值指标）、故障日志（Logs -- 事件记录）。

### 为什么需要可观测性

生产环境中的问题往往是间歇性的、跨组件的。一个请求可能经过网关、认证服务、业务服务、数据库、缓存 -- 任何一个环节出问题都会影响最终结果。没有可观测性，排查问题只能靠猜测和加日志重新部署。有了可观测性，你可以在问题发生时立刻定位到具体环节。

### 没有可观测性会怎样

应用在生产环境中变成"黑盒"。出了问题只能看到最终的错误响应，不知道内部发生了什么。排查问题依赖"加日志 → 重新部署 → 等问题复现"的低效循环。有了可观测性，系统持续输出结构化的遥测数据，问题发生时可以立刻回溯完整的请求路径和上下文。

---

## 概念深入解释

### 三大支柱

```
                    ┌─────────────────────────────────────┐
                    │           可观测性                     │
                    └─────────────────────────────────────┘
                         │           │           │
                    ┌────▼───┐  ┌────▼───┐  ┌────▼───┐
                    │ Traces │  │Metrics │  │  Logs  │
                    │链路追踪 │  │  指标   │  │  日志  │
                    └────────┘  └────────┘  └────────┘
                         │           │           │
                    Jaeger/Tempo  Prometheus   ELK/Loki
```

**Traces（链路追踪）** -- 追踪一个请求从入口到出口的完整路径。

- 一个请求 = 一个 Trace
- Trace 由多个 Span 组成，每个 Span 代表一个操作（如 HTTP 调用、数据库查询、缓存读取）
- Span 之间有父子关系，形成调用树
- 每个 Span 记录：操作名、开始时间、持续时间、状态、属性

**Metrics（指标）** -- 用数值描述系统状态的时间序列数据。

- 计数器（Counter）-- 只增不减，如请求总数、错误总数
- 仪表盘（Gauge）-- 可增可减，如当前活跃连接数、内存使用量
- 直方图（Histogram）-- 值的分布，如请求延迟的 P50/P95/P99
- 典型指标：QPS、错误率、延迟分布、JVM 内存、线程数

**Logs（日志）** -- 离散的事件记录。

- 结构化日志（JSON 格式）比纯文本日志更易于查询和聚合
- 日志应该包含 Trace ID，这样可以把日志和链路追踪关联起来
- 日志级别：TRACE < DEBUG < INFO < WARN < ERROR

### Spring Boot Actuator

Actuator 是 Spring Boot 的运维端点模块，提供应用内部状态的 HTTP 接口：

| 端点 | 路径 | 功能 |
|------|------|------|
| health | `/actuator/health` | 健康检查（数据库连接、磁盘空间等） |
| metrics | `/actuator/metrics` | 应用指标（JVM、HTTP、自定义） |
| info | `/actuator/info` | 应用信息（版本、构建时间） |
| env | `/actuator/env` | 环境配置（注意安全，生产环境应限制） |
| beans | `/actuator/beans` | 容器中所有 Bean 的列表 |
| loggers | `/actuator/loggers` | 动态调整日志级别 |

**引入方式：**

```yaml
# pom.xml 中引入 Starter
# spring-boot-starter-actuator

# application.yml 中配置暴露的端点
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: when-authorized
```

默认只暴露 `health` 端点。`metrics`、`env`、`beans` 等需要显式开放，且生产环境应配合安全策略限制访问。

### OpenTelemetry（OTel）

OpenTelemetry 是 CNCF 的可观测性标准，定义了 Traces、Metrics、Logs 的采集、处理和导出规范。它不是一个具体的后端系统，而是一套协议和 SDK。

**Spring Boot 与 OTel 的集成方式：**

| 方式 | 说明 | 适用场景 |
|------|------|----------|
| OTel Java Agent | 以 `-javaagent` 方式挂载，零代码侵入 | 快速接入，不想改代码 |
| Micrometer + OTel Exporter | Spring Boot 原生集成 Micrometer，通过 OTel 导出 | 需要细粒度控制 |
| Spring Boot 3 Observation API | 基于 Micrometer Observation 统一 API | Spring Boot 3 推荐方式 |

**典型的可观测性技术栈：**

```
Spring Boot App
    ├── Traces → OTel Collector → Jaeger / Tempo
    ├── Metrics → Micrometer → Prometheus → Grafana
    └── Logs → Logback (JSON) → Loki / ELK
```

### Actuator 与 OTel 的关系

两者不是替代关系，而是互补：

| 维度 | Actuator | OpenTelemetry |
|------|----------|---------------|
| 定位 | 应用内部状态暴露 | 遥测数据采集和导出 |
| 输出 | HTTP 端点（JSON） | 导出到外部后端（Jaeger、Prometheus） |
| 粒度 | 应用级（健康、Bean 列表） | 请求级（单个请求的完整链路） |
| 使用场景 | 运维检查、调试 | 生产监控、性能分析、故障排查 |

简单说：Actuator 告诉你"应用现在的状态"，OTel 告诉你"每个请求经历了什么"。

### Micrometer -- Spring 的指标门面

Micrometer 之于 Metrics，就像 SLF4J 之于 Logging -- 它是一个门面（Facade），提供统一的指标 API，底层可以对接不同的监控系统（Prometheus、Datadog、InfluxDB 等）。

Spring Boot Actuator 的 `/actuator/metrics` 端点就是基于 Micrometer 实现的。你也可以用 Micrometer API 定义自定义指标：

```java
@Service
public class OrderService {
    private final Counter orderCounter;

    public OrderService(MeterRegistry registry) {
        this.orderCounter = registry.counter("orders.created");
    }

    public void createOrder(OrderDTO dto) {
        // 业务逻辑...
        orderCounter.increment();
    }
}
```

### 日志体系（SLF4J + Logback）

日志是可观测性三大支柱之一，也是开发者最熟悉的观测手段。Spring Boot 默认使用 SLF4J 作为日志门面，Logback 作为实现。

**日志级别：**

| 级别 | 用途 |
|------|------|
| TRACE | 最细粒度，追踪代码执行路径 |
| DEBUG | 开发调试信息 |
| INFO | 关键业务事件（默认输出级别） |
| WARN | 潜在问题，不影响正常运行 |
| ERROR | 错误，需要关注和处理 |

**结构化日志：**

生产环境中，纯文本日志难以被机器解析和聚合。结构化日志（JSON 格式）让日志系统（ELK、Loki）能高效地索引和查询：

```yaml
# application.yml -- 配置 JSON 格式输出（Spring Boot 3.4+）
logging:
  structured:
    format:
      console: ecs  # 或 logstash
```

Spring Boot 3.4 之前需要通过 `logback-spring.xml` 配置 JSON encoder（如 `logstash-logback-encoder`）。

**日志与链路追踪的关联：**

当集成了 Micrometer Tracing 或 OTel 后，Spring Boot 会自动在日志中注入 Trace ID 和 Span ID。这意味着你可以从一条日志直接跳转到对应的完整链路追踪，反之亦然。

**最佳实践：**

- 使用参数化日志避免字符串拼接开销：`log.info("User {} created order {}", userId, orderId)`
- 不要在日志中输出敏感信息（密码、Token、身份证号）
- 生产环境使用 INFO 级别，需要排查时通过 Actuator 的 `/actuator/loggers` 端点动态调整
- 异常日志传入异常对象而非手动 `toString()`：`log.error("Failed to process", exception)`

---

## 核心要点

1. **可观测性 = Traces + Metrics + Logs。** 三者互补，缺一不可。Traces 看路径，Metrics 看趋势，Logs 看细节。
2. **Actuator 是应用侧的状态暴露。** 健康检查、指标端点、环境信息 -- 是运维的第一入口。
3. **OpenTelemetry 是行业标准。** 不绑定特定后端，一次接入可以导出到任何兼容的监控系统。
4. **日志要结构化，且包含 Trace ID。** 这样才能把日志和链路追踪关联起来，实现跨系统的问题排查。
5. **可观测性应该从项目初期就建立。** 不是"出了问题再加"，而是"从第一天就有"。
6. **用参数化日志、动态调级、JSON 输出。** 这三点是 Spring Boot 日志的核心实践。

---

## 常见误区

- **以为 Actuator 就是全部的可观测性。** Actuator 只提供应用级的状态端点，不提供请求级的链路追踪。完整的可观测性需要 Actuator + Tracing + Metrics + Structured Logging 配合。
- **在生产环境暴露所有 Actuator 端点。** `env` 端点会暴露环境变量（可能包含密码），`beans` 端点暴露内部结构。生产环境应该只暴露 `health` 和 `metrics`，其他端点需要认证保护。
- **只看平均延迟不看分位数。** 平均延迟 50ms 看起来很好，但如果 P99 是 5 秒，意味着 1% 的用户体验极差。应该关注 P95/P99 分位数。
- **日志中不包含请求上下文。** 纯文本日志 `"Order created"` 在生产环境中几乎无法排查问题。应该包含 Trace ID、用户 ID、请求参数等上下文信息。

---

## 与其他概念的关联

- **前置：** [Java Spring Boot 概述](./10_Java%20Spring%20Boot%20概述.md) -- Actuator 是 Spring Boot 的生产就绪特性之一。[Java Spring Boot Starter](./12_Java%20Spring%20Boot%20Starter.md) -- 通过 `spring-boot-starter-actuator` 引入。[Java Spring Boot 配置](./14_Java%20Spring%20Boot%20配置.md) -- Actuator 端点的暴露和安全策略通过配置文件控制。
- **并行：** 无。可观测性是 Part 3 的收束主题，把"跑起来"和"看得见"连接起来。
- **后续：** Part 10 微服务中的分布式链路追踪会在多服务场景下扩展本文的 Tracing 概念。Part 6 的配置管理与 Profile 会涉及日志级别的多环境配置策略。
