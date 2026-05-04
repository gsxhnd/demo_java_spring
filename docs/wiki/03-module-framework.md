# 框架核心模块 (framework)

> Spring 框架体系能力：安全认证、AOP、监控运维、日志、测试、API 文档、ORM、缓存、调度、异步、校验、文件处理

## 设计决策

### 为什么需要这个模块

Spring 不仅仅是 Web 框架，还提供了丰富的企业级能力。开发者需要了解如何整合这些能力来构建生产级应用。本模块覆盖了从安全到运维的完整技术栈。

### 为什么这么设计

- **选择了**：按关注点（安全、监控、测试等）横向切分，每个子主题独立成章
- **而不是**：按纵切（如"构建一个生产级应用"将所有能力混在一起）
- **原因**：横向切分更利于模块化学习，开发者可按需查阅；纵向切分适合教程类内容而非参考文档

## 关键类型与接口

### 子主题与示例项目

| 子主题 | 示例项目 | 核心技术 |
|--------|----------|----------|
| Security + JWT | spring-security-demo | Spring Security, JWT, OAuth2 |
| AOP | 含在 spring-mvc-demo | Spring AOP, AspectJ |
| Actuator | 各项目内置 | Micrometer, Health Check |
| Logging | 各项目内置 | Logback, SLF4J |
| Testing | 各项目含测试 | JUnit 5, Mockito, TestContainers |
| OpenAPI | 各项目内置 | SpringDoc OpenAPI, Swagger UI |
| MyBatis | 含在 spring-mysql-demo | MyBatis-Plus |
| JPA 深入 | spring-jpa-advanced-demo | Specification, 审计, Flyway |
| 缓存体系 | spring-cache-demo | Caffeine + Redis |
| 任务调度 | spring-scheduling-demo | @Scheduled, Quartz |
| 异步处理 | spring-async-demo | @Async, CompletableFuture |
| 数据校验 | 各项目内置 | Bean Validation |
| 文件上传 | spring-file-demo | MultipartFile, MinIO/S3 |

## 模块结构

```text
docs/reference/framework/
├── README.md           # 框架核心篇索引
├── security-jwt.md     # Spring Security + JWT
├── aop.md              # AOP 面向切面编程
├── actuator.md         # 监控运维
├── logging.md          # 日志体系
├── testing.md          # 单元测试
├── openapi.md          # API 文档
├── mybatis.md          # MyBatis vs JPA
├── jpa-advanced.md     # JPA 进阶
├── cache.md            # 多级缓存
├── scheduling.md       # 定时任务
├── async.md            # 异步处理
├── validation.md       # 数据校验
└── file-upload.md      # 文件上传/下载
```

## 与其他模块的关系

### 依赖

- **核心基础模块**：基于 MVC、IoC、事务基础
- **数据库模块**：Cache、JPA 深入依赖数据库连接

### 被依赖

- **微服务模块**：Security（认证授权）、Actuator（健康检查）、Logging（日志）

## 详细技术参考

以下为各子主题的完整技术参考文档，包含核心概念、配置示例、代码片段、进阶要点和常见问题：

| 子主题 | 参考文档 |
|--------|----------|
| Security + JWT | [reference/framework/security-jwt.md](../reference/framework/security-jwt.md) |
| AOP 面向切面编程 | [reference/framework/aop.md](../reference/framework/aop.md) |
| Actuator 监控运维 | [reference/framework/actuator.md](../reference/framework/actuator.md) |
| 日志体系 | [reference/framework/logging.md](../reference/framework/logging.md) |
| 单元测试 | [reference/framework/testing.md](../reference/framework/testing.md) |
| OpenAPI 文档 | [reference/framework/openapi.md](../reference/framework/openapi.md) |
| MyBatis | [reference/framework/mybatis.md](../reference/framework/mybatis.md) |
| JPA 进阶 | [reference/framework/jpa-advanced.md](../reference/framework/jpa-advanced.md) |
| 多级缓存 | [reference/framework/cache.md](../reference/framework/cache.md) |
| 定时任务 | [reference/framework/scheduling.md](../reference/framework/scheduling.md) |
| 异步处理 | [reference/framework/async.md](../reference/framework/async.md) |
| 数据校验 | [reference/framework/validation.md](../reference/framework/validation.md) |
| 文件上传/下载 | [reference/framework/file-upload.md](../reference/framework/file-upload.md) |

## 注意事项

- Security + JWT 是最高频的集成场景，文档必须覆盖 JWT 生成/验证/刷新/注销全流程
- AOP 的代理机制（JDK 动态代理 vs CGLIB）是常见面试题，文档需解释清楚
- `@Async` 的线程池配置和异常处理是生产环境常见问题
- `@Transactional` 和 `@Async` 同时使用的陷阱需要在文档中明确警示
- Spring Cache 抽象层的一致性语义（读时更新 vs 写时更新）需要解释清楚
