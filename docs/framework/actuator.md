# Spring Boot Actuator — 监控运维 / Monitoring & Operations

> 健康检查、指标采集、运行时信息、Prometheus 集成

## 1. 概述 / Overview

Spring Boot Actuator 提供生产级别的监控和管理端点，用于健康检查、指标采集、环境信息查看等。是微服务可观测性的基础组件。

### Actuator 能做什么

| 能力 | 说明 |
|---|---|
| 健康检查 | `/actuator/health` — K8s 存活/就绪探针 |
| 指标采集 | `/actuator/metrics` — JVM、HTTP、数据库等指标 |
| 环境信息 | `/actuator/env` — 配置属性查看 |
| Bean 列表 | `/actuator/beans` — 所有 Spring Bean |
| 日志级别 | `/actuator/loggers` — 运行时动态调整日志级别 |
| 线程转储 | `/actuator/threaddump` — 线程快照 |
| 堆转储 | `/actuator/heapdump` — 堆内存快照 |
| HTTP 追踪 | `/actuator/httpexchanges` — 最近的 HTTP 请求记录 |

---

## 2. 核心概念 / Core Concepts

### 端点分类

| 类别 | 端点 | 说明 |
|---|---|---|
| 健康 | `health` | 应用健康状态（UP/DOWN） |
| 信息 | `info` | 应用信息（版本、Git 信息） |
| 指标 | `metrics`, `prometheus` | 数值指标 |
| 运维 | `loggers`, `threaddump`, `heapdump` | 运行时诊断 |
| 配置 | `env`, `configprops`, `beans` | 配置和 Bean 信息 |
| 管理 | `shutdown` | 优雅关闭（默认禁用） |

### 健康检查指示器

Spring Boot 自动检测并注册以下健康指示器：

| 指示器 | 检测对象 |
|---|---|
| `DataSourceHealthIndicator` | 数据库连接 |
| `RedisHealthIndicator` | Redis 连接 |
| `MongoHealthIndicator` | MongoDB 连接 |
| `ElasticsearchRestClientHealthIndicator` | Elasticsearch 连接 |
| `DiskSpaceHealthIndicator` | 磁盘空间 |
| `RabbitHealthIndicator` | RabbitMQ 连接 |

### Kubernetes 探针

| 探针 | Actuator 端点 | 用途 |
|---|---|---|
| Liveness | `/actuator/health/liveness` | 应用是否存活（失败则重启 Pod） |
| Readiness | `/actuator/health/readiness` | 应用是否就绪（失败则不接收流量） |
| Startup | `/actuator/health/liveness` | 启动检查（慢启动应用） |

---

## 3. 快速集成 / Quick Start

### Maven 依赖

- `spring-boot-starter-actuator` — Actuator 核心
- `micrometer-registry-prometheus` — Prometheus 指标导出（可选）

### 关键配置

| 配置 | 说明 |
|---|---|
| `management.endpoints.web.exposure.include` | 暴露的端点（`*` 全部，或逗号分隔） |
| `management.endpoint.health.show-details` | 健康详情（`always` / `when-authorized`） |
| `management.server.port` | 管理端口（可与业务端口分离） |
| `management.endpoints.web.base-path` | 端点基础路径（默认 `/actuator`） |
| `management.prometheus.metrics.export.enabled` | 启用 Prometheus 导出 |
| `management.endpoint.shutdown.enabled` | 启用优雅关闭端点 |

---

## 4. 进阶要点 / Advanced Topics

- **自定义 HealthIndicator**：实现 `HealthIndicator` 接口，检查自定义依赖（如第三方 API）
- **自定义 Metrics**：通过 `MeterRegistry` 注册 Counter / Gauge / Timer
- **Prometheus 集成**：`/actuator/prometheus` 端点暴露 Prometheus 格式指标
- **Grafana Dashboard**：导入 Spring Boot 社区 Dashboard（ID: 4701、12900）
- **安全加固**：管理端口与业务端口分离，或通过 Spring Security 保护 Actuator 端点
- **Info 端点**：`management.info.git.mode=full` 显示 Git 提交信息
- **动态日志级别**：`POST /actuator/loggers/<logger>` 运行时调整日志级别，无需重启

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| 端点访问 404 | 检查 `management.endpoints.web.exposure.include` 配置 |
| health 只显示 UP/DOWN | 设置 `show-details=always` |
| Prometheus 端点没数据 | 确认引入了 `micrometer-registry-prometheus` |
| 生产环境暴露端点不安全 | 分离管理端口 + Security 保护 |
| heapdump 文件太大 | 正常现象，用 MAT 工具分析 |

---

## 6. 示例项目 / Example

Actuator 配置集成在各示例项目的 `application.yml` 中。

## 7. 参考链接 / References

- [Spring Boot Actuator 官方文档](https://docs.spring.io/spring-boot/reference/actuator/)
- [Micrometer 官方文档](https://micrometer.io/docs)
