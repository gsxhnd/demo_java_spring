# 项目架构

## 项目定位

与零散的博客文章或视频教程不同，本项目提供：

- **结构化学习路径**：从核心基础到微服务进阶的推荐学习路线
- **独立可运行的代码**：每个示例项目都是一个完整的 Spring Boot 应用
- **Docker 一键环境**：所有中间件通过 Docker Compose 统一管理

### 目标用户

| 用户 | 推荐路径 |
|------|---------|
| 初级 Java 开发者 | 核心基础篇快速上手 Spring Boot |
| 中级 Java 开发者 | 数据库/框架篇深入 Spring Data、Security 等 |
| 高级/架构师 | 微服务篇掌握 Spring Cloud 分布式架构 |

### MVP 范围

**包含**：核心基础篇文档 + 示例项目、数据库篇文档、框架核心篇文档、微服务篇文档、Docker Compose 中间件编排

**不包含**：CI/CD 流水线、Web 端在线学习平台、Kubernetes 部署方案

## 架构概述

Monorepo + 独立示例项目架构。根项目为文档中心，`examples/` 下每个子目录为独立的 Spring Boot 项目。

```
┌─────────────────────────────────────────────┐
│          docs/ — 文档层                       │
│   dev/ | reference/                           │
│   reference: core/ | database/ | framework/  │
│              microservice/ | advanced/        │
└──────────────────────┬──────────────────────┘
                       │ 引用对应
                       ▼
┌─────────────────────────────────────────────┐
│         examples/ — 示例代码层               │
│   核心基础篇: 4 个独立项目                    │
│   spring-ioc-demo/ | spring-mvc-demo/       │
│   spring-autoconfig-demo/ |                 │
│   spring-transaction-demo/                  │
└──────────────────────┬──────────────────────┘
                       │ 依赖
                       ▼
┌─────────────────────────────────────────────┐
│    devops/ — 基础设施层                       │
│   MySQL | Redis | MongoDB | ES | ...        │
└─────────────────────────────────────────────┘
```

## 模块职责

| 模块 | 职责 | 入口文件 |
|------|------|----------|
| `docs/dev/` | 开发文档：架构、技术栈、路线图、快速开始、配置、学习指南、故障排查、FAQ | 本文件 |
| `docs/reference/` | 技术参考：各主题深度文档 | `reference/00-readme.md` |
| `examples/` | 独立可运行的示例项目 | `examples/README.md` |
| `devops/` | 中间件容器编排 | `devops/full-stack-compose.yml` |

## 依赖规则

```
docs/ (文档层 — 无代码依赖)
  ↑ 引用
examples/ (示例代码层 — 每个项目独立，无交叉依赖)
  ↑ 运行时依赖
devops/ (基础设施层 — 纯配置文件，无代码)
```

- `examples/` 下每个子目录必须是独立可编译运行的 Spring Boot 项目，禁止子项目间交叉依赖
- 示例项目只通过 `spring-boot-starter-parent` 和 Maven 中央仓库获取依赖
- 中间件通过 Docker Compose 统一提供，示例项目通过 `application.yml` 连接
- 文档中引用的代码片段应与对应示例项目保持一致

## 主题划分

| 主题 | 子文档 |
|------|--------|
| 核心基础篇 | IoC & DI、Spring MVC、自动配置与 Starter、事务管理 |
| 数据库篇 | MySQL、PostgreSQL、Redis、MongoDB、Elasticsearch、ClickHouse、InfluxDB |
| 框架核心篇 | Security + JWT、AOP、Actuator、日志、测试、OpenAPI、MyBatis、JPA 深入、缓存、调度、异步、校验、文件上传 |
| 微服务篇 | Gateway、服务发现、配置中心、服务间通信、熔断降级、分布式事务、消息队列、可观测性 |
| 进阶主题篇 | WebSocket/SSE、Spring Batch、Spring WebFlux、Spring Modulith、Docker 部署 |

## 示例项目约定

- **项目名**：`spring-{topic}-demo`
- **Maven 坐标**：`com.example / spring-{topic}-demo`
- **Parent**：`spring-boot-starter-parent:4.0.5`
- **Java 版本**：21
- **包结构**：`com.example.{topic}/` 下分 `controller/`、`service/`、`repository/`、`entity/`、`dto/`、`config/`、`exception/`
- **入口类**：`{Topic}DemoApplication.java`
- **配置格式**：YAML（`application.yml`）
- **默认端口**：8080

## 中间件服务

通过 Docker Compose 管理，开发环境默认凭据：

| 服务 | 端口 | 用户/密码 |
|------|------|----------|
| MySQL 8.0 | 3306 | root / root123 |
| PostgreSQL 16 | 5432 | postgres / postgres123 |
| Redis 7 | 6379 | password: redis123 |
| MongoDB 7 | 27017 | root / mongo123 |
| Elasticsearch 8.15 | 9200 | (security disabled) |
| ClickHouse | 8123 | default / clickhouse123 |
| InfluxDB 2.7 | 8086 | admin / admin12345678 |

所有服务连接数据库 `demo_db`（InfluxDB 除外：bucket `monitoring`，org `my-org`）。

## 运行时模型

- 各示例项目为独立的 Spring Boot 应用，通过内嵌 Tomcat/Netty 提供 HTTP 服务
- 默认端口 8080（可通过 `application.yml` 修改）
- 中间件通过 Docker 容器运行，项目启动时自动连接
- 无共享状态，各示例项目可并行运行（需注意端口冲突）

## 错误处理策略

- 示例项目遵循 Spring Boot 标准错误处理机制，通过 `@ControllerAdvice` 统一处理异常
- 中间件不可用时，应用启动失败并给出明确错误信息
