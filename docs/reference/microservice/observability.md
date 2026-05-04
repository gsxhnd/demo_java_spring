# Observability — 可观测性 / Observability

> OpenTelemetry + Micrometer：Traces、Metrics、Logs 三大支柱

## 1. 概述 / Overview

可观测性（Observability）是微服务运维的核心能力，由三大支柱组成：

| 支柱 | 说明 | 回答的问题 |
|---|---|---|
| **Traces（链路追踪）** | 跟踪一个请求在多个服务间的完整调用链 | "这个请求经过了哪些服务？哪里慢了？" |
| **Metrics（指标）** | 数值型时序数据（QPS、延迟、错误率） | "系统现在的状态如何？趋势怎样？" |
| **Logs（日志）** | 离散的事件记录 | "发生了什么？为什么出错？" |

### 为什么选择 OpenTelemetry

OpenTelemetry (OTel) 是 CNCF 的可观测性标准，统一了 Traces、Metrics、Logs 的采集规范，避免厂商锁定。

```
┌─────────────────────────────────────────────────┐
│              Application (Spring Boot)           │
│                                                  │
│  Micrometer ──→ OTel Metrics ──→ Prometheus     │
│  OTel SDK   ──→ OTel Traces  ──→ Jaeger/Tempo  │
│  SLF4J      ──→ OTel Logs    ──→ Loki          │
│                                                  │
└──────────────────────┬──────────────────────────┘
                       │ OTLP Protocol
                       ▼
              ┌─────────────────┐
              │ OTel Collector  │  ← 统一采集、处理、导出
              │ (可选)          │
              └────────┬────────┘
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
     Prometheus    Jaeger/Tempo    Loki
     (Metrics)     (Traces)       (Logs)
          │            │            │
          └────────────┼────────────┘
                       ▼
                   Grafana (统一可视化)
```

### 可观测性方案对比

| 方案 | Traces | Metrics | Logs | 说明 |
|---|---|---|---|---|
| **OpenTelemetry + Grafana Stack** | Tempo | Prometheus | Loki | 开源首选，CNCF 标准 |
| SkyWalking | 内置 | 内置 | 内置 | 国产 APM，开箱即用 |
| Zipkin | Zipkin | - | - | 仅链路追踪，轻量 |
| ELK Stack | - | - | Elasticsearch | 仅日志，重量级 |
| 商业 APM | 全部 | 全部 | 全部 | Datadog / New Relic / Dynatrace |

---

## 2. 核心概念 / Core Concepts

### Traces 链路追踪

```
Client Request
  │
  ▼
Gateway (Span 1) ──────────────────────────────────────
  │                                                    │
  ▼                                                    │
User Service (Span 2) ─────────────────                │
  │                                   │                │
  ▼                                   │                │
Order Service (Span 3) ──────         │                │
  │                         │         │                │
  ▼                         │         │                │
MySQL Query (Span 4) ──     │         │                │
                       │    │         │                │
                       ▼    ▼         ▼                ▼
Timeline: ─────────────────────────────────────────────→
          0ms   10ms  30ms  50ms  80ms  100ms
```

| 概念 | 说明 |
|---|---|
| Trace | 一个完整请求的调用链（由多个 Span 组成） |
| Span | 调用链中的一个操作单元（一次 RPC、一次 DB 查询） |
| Trace ID | 全局唯一标识，贯穿整个调用链 |
| Span ID | 单个 Span 的标识 |
| Parent Span ID | 父 Span，形成调用树 |
| Baggage | 跨服务传递的上下文数据 |

### Metrics 指标

| 指标类型 | 说明 | 示例 |
|---|---|---|
| Counter | 只增不减的计数器 | 请求总数、错误总数 |
| Gauge | 可增可减的瞬时值 | 当前连接数、内存使用量 |
| Histogram | 分布统计（分桶） | 请求延迟分布（p50/p95/p99） |
| Summary | 分位数统计 | 类似 Histogram，客户端计算 |

### 关键指标（RED / USE）

| 方法 | 指标 | 适用 |
|---|---|---|
| RED | Rate（请求率）、Errors（错误率）、Duration（延迟） | 服务接口 |
| USE | Utilization（利用率）、Saturation（饱和度）、Errors（错误） | 基础设施 |

---

## 3. 快速集成 / Quick Start

### Spring Boot 4.x + Micrometer + OTel

Spring Boot 4.x 内置 Micrometer 作为指标门面，通过 Micrometer Tracing 桥接 OpenTelemetry。

- 依赖：
  - `spring-boot-starter-actuator` — Actuator 基础
  - `micrometer-tracing-bridge-otel` — Micrometer → OTel 桥接
  - `opentelemetry-exporter-otlp` — OTLP 导出器
  - `micrometer-registry-prometheus` — Prometheus 指标导出

- 关键配置：

| 配置 | 说明 |
|---|---|
| `management.tracing.sampling.probability` | 采样率（1.0 = 100%，生产建议 0.1） |
| `management.otlp.tracing.endpoint` | OTel Collector OTLP 端点 |
| `management.otlp.metrics.export.url` | Metrics OTLP 端点 |
| `management.endpoints.web.exposure.include` | 暴露的 Actuator 端点 |
| `management.prometheus.metrics.export.enabled` | 启用 Prometheus 导出 |

### OTel Collector 部署

OTel Collector 是可选的中间层，负责接收、处理、导出遥测数据：

| 组件 | 说明 |
|---|---|
| Receivers | 接收数据（OTLP、Jaeger、Zipkin 格式） |
| Processors | 处理数据（批量、过滤、采样） |
| Exporters | 导出数据（Prometheus、Jaeger、Tempo、Loki） |

---

## 4. 进阶要点 / Advanced Topics

- **自动埋点**：Spring Boot 自动为 HTTP 请求、RestTemplate、WebClient、JDBC、Redis、Kafka 等生成 Span
- **手动埋点**：`Tracer.spanBuilder("custom-operation")` 创建自定义 Span
- **Trace ID 传递**：通过 HTTP Header（`traceparent`）自动传递，Feign/RestTemplate/WebClient 自动注入
- **日志关联**：MDC 中自动注入 `traceId` 和 `spanId`，日志中可搜索关联
- **Baggage 传播**：跨服务传递业务上下文（用户 ID、租户 ID）
- **自定义 Metrics**：`MeterRegistry.counter()` / `.gauge()` / `.timer()` 注册自定义指标
- **Grafana Dashboard**：导入社区 Dashboard 模板，快速搭建监控面板
- **告警规则**：Prometheus AlertManager / Grafana Alerting 配置告警

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| Trace ID 跨服务断裂 | 确认 Feign/RestTemplate 的 Interceptor 正确传递 `traceparent` Header |
| 采样率设置 | 开发环境 1.0（全采样），生产环境 0.01-0.1 |
| 日志中没有 traceId | 确认 Logback pattern 包含 `%X{traceId}` |
| Prometheus 指标太多 | 配置 `management.metrics.enable.*` 按需开启 |
| OTel Collector 内存高 | 调整 batch processor 的 `send_batch_size` 和 `timeout` |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-microservice-demo/`](../../examples/spring-microservice-demo/)（待生成）

## 7. 参考链接 / References

- [OpenTelemetry 官方文档](https://opentelemetry.io/docs/)
- [Micrometer 官方文档](https://micrometer.io/docs)
- [Spring Boot Observability](https://docs.spring.io/spring-boot/reference/actuator/observability.html)
- [Grafana Stack (Tempo + Loki + Prometheus)](https://grafana.com/docs/)
