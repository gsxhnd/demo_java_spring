# 框架核心篇 / Spring Boot Framework Essentials

> Spring Boot 核心能力：安全、AOP、监控、日志、测试、API 文档、ORM、缓存、调度、异步、校验、文件处理

## 概览 / Overview

本篇覆盖 Spring Boot 开发中最常用的框架级能力，这些能力贯穿单体和微服务架构。

```
┌─────────────────────────────────────────────────┐
│                Spring Boot Application           │
│                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │ Security │  │   AOP    │  │ Actuator │      │
│  │ + JWT    │  │ 切面编程  │  │ 监控运维  │      │
│  └──────────┘  └──────────┘  └──────────┘      │
│                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │ Logging  │  │ Testing  │  │ OpenAPI  │      │
│  │ 日志体系  │  │ 单元测试  │  │ API 文档  │      │
│  └──────────┘  └──────────┘  └──────────┘      │
│                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │ MyBatis  │  │ JPA 深入  │  │  Cache   │      │
│  │ ORM 框架  │  │ 动态查询  │  │ 多级缓存  │      │
│  └──────────┘  └──────────┘  └──────────┘      │
│                                                  │
│  ┌──────────────────────┐  ┌──────────────────┐ │
│  │ Scheduling 任务调度   │  │ Async 异步处理    │ │
│  └──────────────────────┘  └──────────────────┘ │
│                                                  │
│  ┌──────────┐  ┌──────────────────────────────┐ │
│  │Validation│  │ File Upload/Download         │ │
│  │ 数据校验  │  │ 文件上传/下载                 │ │
│  └──────────┘  └──────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

---

## 文档列表 / Documents

| 文档 | 主题 | 关键词 |
|---|---|---|
| [Security + JWT](security-jwt.md) | 认证授权 | Filter Chain, JWT, OAuth2, RBAC |
| [AOP](aop.md) | 面向切面编程 | Aspect, Pointcut, Advice, 日志/权限/事务 |
| [Actuator](actuator.md) | 监控运维 | Health, Metrics, Info, Prometheus |
| [Logging](logging.md) | 日志体系 | SLF4J, Logback, Log4j2, MDC, JSON Log |
| [Testing](testing.md) | 单元测试 | JUnit 5, Mockito, TestContainers, MockMvc |
| [OpenAPI](openapi.md) | API 文档 | SpringDoc, Swagger UI, OpenAPI 3.0 |
| [MyBatis](mybatis.md) | ORM 框架 | MyBatis, MyBatis-Plus, JPA 对比 |
| [JPA 深入](jpa-advanced.md) | JPA 进阶 | Specification, 审计, 多数据源, Flyway |
| [缓存体系](cache.md) | 多级缓存 | Spring Cache, Caffeine, Redis L1+L2 |
| [任务调度](scheduling.md) | 定时任务 | @Scheduled, Quartz, XXL-Job |
| [异步处理](async.md) | 异步与线程池 | @Async, CompletableFuture, Spring Event |
| [数据校验](validation.md) | Bean Validation | @Valid, @Validated, 分组校验, 自定义校验器 |
| [文件上传/下载](file-upload.md) | 文件处理 | MultipartFile, 流式上传, MinIO/S3 |

## 示例项目 / Example

- Security + JWT 示例 → [`examples/spring-security-demo/`](../../examples/spring-security-demo/) ✅
- JPA 进阶示例 → [`examples/spring-jpa-advanced-demo/`](../../examples/spring-jpa-advanced-demo/) ✅
- 缓存示例 → [`examples/spring-cache-demo/`](../../examples/spring-cache-demo/) ✅
- 任务调度示例 → [`examples/spring-scheduling-demo/`](../../examples/spring-scheduling-demo/) ✅
- 异步处理示例 → [`examples/spring-async-demo/`](../../examples/spring-async-demo/) ✅
- 文件上传下载示例 → [`examples/spring-file-demo/`](../../examples/spring-file-demo/) ✅
- 其他能力集成在各数据库/微服务示例项目中
