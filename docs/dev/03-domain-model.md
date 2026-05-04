# 领域模型

## 概述

本文档定义 Spring 生态学习项目的核心领域概念和模块划分。

## 核心概念

### 主题（Topic）

项目的顶层分类，每个主题包含若干子文档。

- **核心基础篇**：IoC & DI、Spring MVC、自动配置与 Starter、事务管理
- **数据库篇**：MySQL、PostgreSQL、Redis、MongoDB、Elasticsearch、ClickHouse、InfluxDB
- **框架核心篇**：Security + JWT、AOP、Actuator、日志、测试、OpenAPI、MyBatis、JPA 深入、缓存、调度、异步、校验、文件上传
- **微服务篇**：Gateway、服务发现、配置中心、服务间通信、熔断降级、分布式事务、消息队列、可观测性
- **进阶主题篇**：WebSocket/SSE、Spring Batch、Spring WebFlux、Spring Modulith、Docker 部署

### 示例项目（Example）

与文档主题对应的独立可运行的 Spring Boot 项目。

- **项目名**：`spring-{主题}-demo`（如 `spring-mysql-demo`）
- **group/artifact**：`com.example / spring-{主题}-demo`
- **Java 版本**：21（当前部分暂为 17）
- **Spring Boot 版本**：4.0.5（当前部分暂为 3.5.0）
- **构建工具**：Maven 3.9+

### 中间件服务（Middleware）

通过 Docker Compose 管理的第三方服务。

- MySQL 8.0 (3306)
- PostgreSQL 16 (5432)
- Redis 7 (6379)
- MongoDB 7 (27017)
- Elasticsearch 8.15 (9200)
- ClickHouse (8123/9000)
- InfluxDB 2.7 (8086)
- RabbitMQ / Kafka（供微服务篇使用）

## 数据流

```text
开发者阅读文档 → 了解技术原理 → 运行对应示例项目 → 查看代码 + 调试
                                             │
                                      Docker Compose
                                             │
                                   中间件服务容器集群
```

## 状态管理

- 文档内容为静态 Markdown，无运行时状态
- 示例项目状态由各项目自行管理（数据库持久化、内存缓存等）
- 中间件数据通过 Docker Volume 持久化
- 版本矩阵（Spring Boot / Java / Spring Cloud 版本）在根 `README.md` 和 `docs/README.md` 中维护
