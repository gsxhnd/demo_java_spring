# 框架核心篇

> Spring Boot 核心能力：安全、AOP、监控、日志、测试、API 文档、ORM、缓存、调度、异步、校验、文件处理。

## 为什么需要这篇

Spring 不仅仅是 Web 框架，还提供了丰富的企业级能力。开发者需要了解如何整合这些能力来构建生产级应用。本篇覆盖了从安全到运维的完整技术栈。

按关注点（安全、监控、测试等）横向切分，每个子主题独立成章，更利于模块化学习。

## 概览

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
│  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │Scheduling│  │  Async   │  │Validation│      │
│  │ 任务调度  │  │ 异步处理  │  │ 数据校验  │      │
│  └──────────┘  └──────────┘  └──────────┘      │
│                                                  │
│  ┌──────────────────────────────────────────┐   │
│  │ File Upload/Download 文件上传/下载         │   │
│  └──────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
```

## 文档列表

| 文档 | 主题 | 关键词 |
|---|---|---|
| [Security + JWT](05-security-jwt.md) | 认证授权 | Filter Chain, JWT, OAuth2, RBAC |
| [AOP](03-aop.md) | 面向切面编程 | Aspect, Pointcut, Advice, 日志/权限/事务 |
| [Actuator](01-actuator.md) | 监控运维 | Health, Metrics, Info, Prometheus |
| [Logging](02-logging.md) | 日志体系 | SLF4J, Logback, Log4j2, MDC, JSON Log |
| [Testing](04-testing.md) | 单元测试 | JUnit 5, Mockito, TestContainers, MockMvc |
| [OpenAPI](06-openapi.md) | API 文档 | SpringDoc, Swagger UI, OpenAPI 3.0 |
| [MyBatis](07-mybatis.md) | ORM 框架 | MyBatis, MyBatis-Plus, JPA 对比 |
| [JPA 进阶](08-jpa-advanced.md) | JPA 深入 | Specification, 审计, 多数据源, Flyway |
| [缓存体系](09-cache.md) | 多级缓存 | Spring Cache, Caffeine, Redis L1+L2 |
| [任务调度](10-scheduling.md) | 定时任务 | @Scheduled, Quartz, XXL-Job |
| [异步处理](11-async.md) | 异步与线程池 | @Async, CompletableFuture, Spring Event |
| [数据校验](12-validation.md) | Bean Validation | @Valid, @Validated, 分组校验, 自定义校验器 |
| [文件上传/下载](13-file-upload.md) | 文件处理 | MultipartFile, 流式上传, MinIO/S3 |

## 示例项目

| 示例项目 | 对应子主题 | 核心技术 |
|---------|----------|----------|
| `examples/spring-security-demo/` | Security + JWT | Spring Security, JWT, OAuth2 |
| `examples/spring-jpa-advanced-demo/` | JPA 深入 | Specification, 审计, Flyway |
| `examples/spring-cache-demo/` | 缓存体系 | Caffeine + Redis |
| `examples/spring-scheduling-demo/` | 任务调度 | @Scheduled, Quartz |
| `examples/spring-async-demo/` | 异步处理 | @Async, CompletableFuture |
| `examples/spring-file-demo/` | 文件上传 | MultipartFile, MinIO/S3 |

AOP、Actuator、Logging、Testing、OpenAPI、MyBatis、Validation 等能力集成在各示例项目中。

## 与其他篇章的关系

- **依赖**：[核心基础篇](../01-core/README.md)（MVC、IoC、事务基础）、[数据库篇](../02-database/README.md)（Cache、JPA 深入依赖数据库连接）
- **被依赖**：[微服务篇](../04-microservice/README.md)（Security 认证授权、Actuator 健康检查、Logging 日志）

## 注意事项

- Security + JWT 是最高频的集成场景，文档覆盖 JWT 生成/验证/刷新/注销全流程
- AOP 的代理机制（JDK 动态代理 vs CGLIB）是常见面试题
- `@Async` 的线程池配置和异常处理是生产环境常见问题
- `@Transactional` 和 `@Async` 同时使用的陷阱需要注意
- Spring Cache 抽象层的一致性语义（读时更新 vs 写时更新）需要理解清楚
