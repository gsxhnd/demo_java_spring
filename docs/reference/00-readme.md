# 技术参考文档

## 概述

本目录包含 Spring 生态学习项目的所有详细技术参考文档。每个文档深入覆盖一个技术主题，包含核心概念、配置示例、代码片段、进阶要点和常见问题。

> 如需了解各模块的设计决策和模块关系，请参阅 [wiki/](../wiki/00-readme.md)。
> 如需快速上手和使用指南，请参阅 [usage/](../usage/00-readme.md)。

## 文档索引

### 核心基础篇 (core)

| 文档 | 主题 | 核心技术栈 |
|------|------|-----------|
| [IoC & DI](./core/ioc-di.md) | IoC 容器与依赖注入 | ApplicationContext, @Component, @Autowired, Bean Lifecycle |
| [Spring MVC](./core/spring-mvc.md) | Web 开发基础 | @RestController, @Valid, @ControllerAdvice, Interceptor |
| [自动配置与 Starter](./core/auto-configuration.md) | Spring Boot 核心机制 | @SpringBootApplication, @ConfigurationProperties, Profile |
| [事务管理](./core/transaction.md) | 声明式事务 | @Transactional, Propagation, Isolation, 事务失效场景 |

### 数据库篇 (database)

| 文档 | 数据库 | 核心技术栈 |
|------|--------|-----------|
| [MySQL](./database/mysql.md) | 关系型 | Spring Data JPA, MyBatis, HikariCP |
| [PostgreSQL](./database/postgresql.md) | 关系型 | Spring Data JPA, JSONB, Full-Text Search |
| [Redis](./database/redis.md) | 缓存 / KV | Spring Data Redis, Lettuce, RedisTemplate |
| [MongoDB](./database/mongodb.md) | 文档型 | Spring Data MongoDB, MongoTemplate |
| [Elasticsearch](./database/elasticsearch.md) | 搜索引擎 | Spring Data Elasticsearch, RestClient |
| [ClickHouse](./database/clickhouse.md) | 列式分析 | JDBC, ClickHouse Java Client |
| [InfluxDB](./database/influxdb.md) | 时序数据库 | InfluxDB Java Client, Flux Query |

### 框架核心篇 (framework)

| 文档 | 主题 | 核心技术栈 |
|------|------|-----------|
| [Security + JWT](./framework/security-jwt.md) | 认证授权 | Spring Security, JWT, OAuth2 |
| [AOP](./framework/aop.md) | 面向切面编程 | Spring AOP, AspectJ |
| [Actuator](./framework/actuator.md) | 监控运维 | Spring Boot Actuator, Micrometer |
| [Logging](./framework/logging.md) | 日志体系 | Logback, Log4j2, SLF4J |
| [Testing](./framework/testing.md) | 单元测试 | JUnit 5, Mockito, TestContainers |
| [OpenAPI](./framework/openapi.md) | API 文档 | SpringDoc OpenAPI, Swagger UI |
| [MyBatis](./framework/mybatis.md) | ORM 框架 | MyBatis, MyBatis-Plus vs JPA |
| [JPA 进阶](./framework/jpa-advanced.md) | JPA 深入 | Specification, 审计, 多数据源, Flyway |
| [缓存体系](./framework/cache.md) | 多级缓存 | Spring Cache, Caffeine, Redis |
| [任务调度](./framework/scheduling.md) | 定时任务 | @Scheduled, Quartz, XXL-Job |
| [异步处理](./framework/async.md) | 异步与线程池 | @Async, CompletableFuture, Spring Event |
| [数据校验](./framework/validation.md) | Bean Validation | @Valid, @Validated, 分组校验, 自定义校验器 |
| [文件上传/下载](./framework/file-upload.md) | 文件处理 | MultipartFile, 流式上传, MinIO/S3 |

### 微服务篇 (microservice)

| 文档 | 主题 | 核心技术栈 |
|------|------|-----------|
| [Gateway](./microservice/gateway.md) | API 网关 | Spring Cloud Gateway, Route, Filter |
| [Service Discovery](./microservice/service-discovery.md) | 服务注册与发现 | Nacos, Eureka, Consul |
| [Config Center](./microservice/config-center.md) | 配置中心 | Nacos Config, Spring Cloud Config |
| [Service Communication](./microservice/service-communication.md) | 服务间通信 | OpenFeign, gRPC |
| [Circuit Breaker](./microservice/circuit-breaker.md) | 熔断降级 | Resilience4j, Sentinel |
| [Distributed Transaction](./microservice/distributed-transaction.md) | 分布式事务 | Seata |
| [Message Queue](./microservice/message-queue.md) | 消息队列 | RabbitMQ, Kafka |
| [Observability](./microservice/observability.md) | 可观测性 | OpenTelemetry, Micrometer |

### 进阶主题篇 (advanced)

| 文档 | 主题 | 核心技术栈 |
|------|------|-----------|
| [WebSocket / SSE](./advanced/websocket.md) | 实时通信 | WebSocket, STOMP, SseEmitter |
| [Spring Batch](./advanced/batch.md) | 批处理 | Job, Step, ItemReader, ItemWriter |
| [Spring WebFlux](./advanced/webflux.md) | 响应式编程 | Reactor, Mono/Flux, R2DBC, WebClient |
| [Spring Modulith](./advanced/modulith.md) | 模块化单体 | ApplicationModule, Event Publication |
| [Docker 部署](./advanced/docker-deploy.md) | 容器化部署 | Dockerfile, 多阶段构建, JVM 调优, docker-compose |

## 学习路线建议

```
核心基础篇 → 数据库篇 → 框架核心篇 → 微服务篇 → 进阶主题篇
```

详细学习路径请参阅 [dev/05-roadmap.md](../dev/05-roadmap.md)。

## 文档规范

- 每个文档包含：概述 → 核心概念 → 快速集成 → 进阶要点 → 常见问题 → 示例项目 → 参考链接
- 中英对照，技术术语保持英文
- 配置示例与 Docker Compose 保持一致
