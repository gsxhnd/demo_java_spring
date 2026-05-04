# Logging — 日志体系 / Logging System

> SLF4J + Logback / Log4j2：日志配置、MDC、JSON 结构化日志

## 1. 概述 / Overview

日志是应用运维和问题排查的基础。Spring Boot 默认使用 SLF4J 作为日志门面，Logback 作为日志实现。

### Java 日志体系全景

```
应用代码
  │
  ▼
日志门面 (Facade)          ← 统一 API，不绑定实现
  SLF4J / JCL
  │
  ▼
日志实现 (Implementation)  ← 实际输出日志
  Logback (默认) / Log4j2
  │
  ▼
输出目标 (Appender)
  Console / File / JSON / ELK / Loki
```

### Logback vs Log4j2

| 特性 | Logback | Log4j2 |
|---|---|---|
| 默认 | Spring Boot 默认 | 需手动切换 |
| 性能 | 高 | 更高（异步 Logger 性能优势明显） |
| 异步日志 | AsyncAppender | 原生 AsyncLogger（基于 Disruptor） |
| 配置文件 | `logback-spring.xml` | `log4j2-spring.xml` |
| 热更新 | 支持 | 支持 |
| GC 压力 | 中 | 低（Garbage-free 模式） |
| 推荐 | 通用场景 | 高吞吐量、低延迟场景 |

---

## 2. 核心概念 / Core Concepts

### 日志级别

| 级别 | 说明 | 使用场景 |
|---|---|---|
| `TRACE` | 最细粒度 | 框架内部调试 |
| `DEBUG` | 调试信息 | 开发环境 |
| `INFO` | 业务信息 | 正常业务流程记录 |
| `WARN` | 警告 | 潜在问题，不影响功能 |
| `ERROR` | 错误 | 异常、失败，需要关注 |

### MDC（Mapped Diagnostic Context）

MDC 是线程级别的上下文，用于在日志中自动附加额外信息：

| MDC Key | 用途 |
|---|---|
| `traceId` | 链路追踪 ID（OpenTelemetry 自动注入） |
| `spanId` | Span ID |
| `requestId` | 请求唯一标识 |
| `userId` | 当前用户 ID |

### Logback 配置结构

| 元素 | 说明 |
|---|---|
| `<appender>` | 输出目标（Console、File、RollingFile） |
| `<encoder>` / `<layout>` | 日志格式（Pattern 或 JSON） |
| `<logger>` | 包/类级别的日志配置 |
| `<root>` | 根 Logger 配置 |
| `<springProfile>` | 按 Spring Profile 条件配置 |

---

## 3. 快速集成 / Quick Start

### Logback（默认，无需额外依赖）

- 配置文件：`src/main/resources/logback-spring.xml`
- 关键配置项（`application.yml`）：

| 配置 | 说明 |
|---|---|
| `logging.level.root` | 根日志级别 |
| `logging.level.<package>` | 包级别日志级别 |
| `logging.file.name` | 日志文件路径 |
| `logging.file.max-size` | 单文件最大大小 |
| `logging.file.max-history` | 保留天数 |
| `logging.pattern.console` | 控制台日志格式 |
| `logging.pattern.file` | 文件日志格式 |

### 切换到 Log4j2

- 排除 `spring-boot-starter-logging`
- 引入 `spring-boot-starter-log4j2`
- 配置文件：`log4j2-spring.xml`

### JSON 结构化日志

- 依赖：`net.logstash.logback:logstash-logback-encoder`（Logback）
- 输出格式：每行一个 JSON 对象，便于 ELK / Loki 解析

---

## 4. 进阶要点 / Advanced Topics

- **RollingFileAppender**：按日期/大小滚动日志文件，配置保留策略
- **异步日志**：Logback `AsyncAppender` 或 Log4j2 `AsyncLogger`，减少日志 IO 对业务的影响
- **JSON 结构化日志**：`LogstashEncoder` 输出 JSON，便于日志平台解析
- **MDC 传递**：HTTP Filter 中设置 MDC（requestId、userId），日志自动携带
- **跨线程 MDC**：异步线程（`@Async`、线程池）中 MDC 会丢失，需用 `MDCAdapter` 或 `TaskDecorator` 传递
- **动态日志级别**：通过 Actuator `/actuator/loggers` 端点运行时调整
- **日志脱敏**：自定义 Converter 或 Filter，对敏感信息（手机号、身份证）脱敏
- **ELK 集成**：Logback → Logstash（TCP/File）→ Elasticsearch → Kibana
- **Loki 集成**：Logback → Loki Appender 或 Promtail 采集日志文件

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| 日志不输出 | 检查日志级别配置，DEBUG 日志默认不输出 |
| logback-spring.xml 不生效 | 文件名必须是 `logback-spring.xml`（不是 `logback.xml`） |
| 异步线程日志没有 traceId | 使用 `TaskDecorator` 传递 MDC |
| 日志文件太大 | 配置 RollingFileAppender + 保留策略 |
| Log4j2 和 Logback 冲突 | 确认排除了 `spring-boot-starter-logging` |

---

## 6. 示例项目 / Example

日志配置集成在各示例项目的 `logback-spring.xml` 中。

## 7. 参考链接 / References

- [Spring Boot Logging 官方文档](https://docs.spring.io/spring-boot/reference/features/logging.html)
- [Logback 官方文档](https://logback.qos.ch/manual/)
- [Log4j2 官方文档](https://logging.apache.org/log4j/2.x/)
- [Logstash Logback Encoder](https://github.com/logfellow/logstash-logback-encoder)
