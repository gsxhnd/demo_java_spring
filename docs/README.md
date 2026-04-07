# Spring 生态学习文档 / Spring Ecosystem Learning Docs

> 基于 Spring Boot 4.0.5 + Spring Cloud 2025.1 (Oakwood) + Java 21

## 版本矩阵 / Version Matrix

| Component | Version | Notes |
|---|---|---|
| Java | 21 (LTS) | Spring Cloud Alibaba 2025.1 要求 Java 21+ |
| Spring Boot | 4.0.5 | 基于 Spring Framework 7.0.6 |
| Spring Framework | 7.0.6 | |
| Spring Cloud | 2025.1 (Oakwood) | 对应 Spring Boot 4.0.x |
| Spring Cloud Alibaba | 2025.1.0.0 | Nacos 3.0 / Sentinel 2.0 / Seata 2.0 |
| Maven | 3.9+ | 构建工具 |

---

## 快速开始 / Getting Started

**[→ 快速开始指南](getting-started.md)** — 环境准备、克隆项目、运行第一个示例、推荐学习路径

---

## 目录 / Table of Contents

### 〇、核心基础篇 / Core Fundamentals

> [核心基础篇索引 →](core/README.md)

| 文档 | 主题 | 核心技术栈 |
|---|---|---|
| [IoC & DI](core/ioc-di.md) | IoC 容器与依赖注入 | ApplicationContext, @Component, @Autowired, Bean Lifecycle |
| [Spring MVC](core/spring-mvc.md) | Web 开发基础 | @RestController, @Valid, @ControllerAdvice, Interceptor |
| [自动配置与 Starter](core/auto-configuration.md) | Spring Boot 核心机制 | @SpringBootApplication, @ConfigurationProperties, Profile |
| [事务管理](core/transaction.md) | 声明式事务 | @Transactional, Propagation, Isolation, 事务失效场景 |

### 一、数据库篇 / Database

> [数据库篇索引 →](database/README.md)

| 文档 | 数据库 | 核心技术栈 |
|---|---|---|
| [MySQL](database/mysql.md) | 关系型 | Spring Data JPA, MyBatis, HikariCP |
| [PostgreSQL](database/postgresql.md) | 关系型 | Spring Data JPA, JSONB, Full-Text Search |
| [Redis](database/redis.md) | 缓存 / KV | Spring Data Redis, Lettuce, RedisTemplate |
| [MongoDB](database/mongodb.md) | 文档型 | Spring Data MongoDB, MongoTemplate |
| [Elasticsearch](database/elasticsearch.md) | 搜索引擎 | Spring Data Elasticsearch, RestClient |
| [ClickHouse](database/clickhouse.md) | 列式分析 | JDBC, ClickHouse Java Client |
| [InfluxDB](database/influxdb.md) | 时序数据库 | InfluxDB Java Client, Flux Query |

### 二、微服务篇 / Microservice

> [微服务篇索引 →](microservice/README.md)

| 文档 | 主题 | 核心技术栈 |
|---|---|---|
| [Gateway](microservice/gateway.md) | API 网关 | Spring Cloud Gateway, Route, Filter |
| [Service Discovery](microservice/service-discovery.md) | 服务注册与发现 | Nacos, Eureka, Consul |
| [Config Center](microservice/config-center.md) | 配置中心 | Nacos Config, Spring Cloud Config |
| [Service Communication](microservice/service-communication.md) | 服务间通信 | OpenFeign, gRPC |
| [Circuit Breaker](microservice/circuit-breaker.md) | 熔断降级 | Resilience4j, Sentinel |
| [Distributed Transaction](microservice/distributed-transaction.md) | 分布式事务 | Seata |
| [Message Queue](microservice/message-queue.md) | 消息队列 | RabbitMQ, Kafka |
| [Observability](microservice/observability.md) | 可观测性 | OpenTelemetry, Micrometer |

### 三、框架核心篇 / Framework

> [框架篇索引 →](framework/README.md)

| 文档 | 主题 | 核心技术栈 |
|---|---|---|
| [Security + JWT](framework/security-jwt.md) | 认证授权 | Spring Security, JWT, OAuth2 |
| [AOP](framework/aop.md) | 面向切面编程 | Spring AOP, AspectJ |
| [Actuator](framework/actuator.md) | 监控运维 | Spring Boot Actuator, Micrometer |
| [Logging](framework/logging.md) | 日志体系 | Logback, Log4j2, SLF4J |
| [Testing](framework/testing.md) | 单元测试 | JUnit 5, Mockito, TestContainers |
| [OpenAPI](framework/openapi.md) | API 文档 | SpringDoc OpenAPI, Swagger UI |
| [MyBatis](framework/mybatis.md) | ORM 框架 | MyBatis, MyBatis-Plus vs JPA |
| [JPA 深入](framework/jpa-advanced.md) | JPA 进阶 | Specification, 审计, 多数据源, Flyway |
| [缓存体系](framework/cache.md) | 多级缓存 | Spring Cache, Caffeine, Redis |
| [任务调度](framework/scheduling.md) | 定时任务 | @Scheduled, Quartz, XXL-Job |
| [异步处理](framework/async.md) | 异步与线程池 | @Async, CompletableFuture, Spring Event |
| [数据校验](framework/validation.md) | Bean Validation | @Valid, @Validated, 分组校验, 自定义校验器 |
| [文件上传/下载](framework/file-upload.md) | 文件处理 | MultipartFile, 流式上传, MinIO/S3 |

### 四、进阶主题篇 / Advanced Topics

> [进阶主题篇索引 →](advanced/README.md)

| 文档 | 主题 | 核心技术栈 |
|---|---|---|
| [WebSocket / SSE](advanced/websocket.md) | 实时通信 | WebSocket, STOMP, SseEmitter |
| [Spring Batch](advanced/batch.md) | 批处理 | Job, Step, ItemReader, ItemWriter |
| [Spring WebFlux](advanced/webflux.md) | 响应式编程 | Reactor, Mono/Flux, R2DBC, WebClient |
| [Spring Modulith](advanced/modulith.md) | 模块化单体 | ApplicationModule, Event Publication |
| [Docker 部署](advanced/docker-deploy.md) | 容器化部署 | Dockerfile, 多阶段构建, JVM 调优, docker-compose |

### 五、示例项目 / Examples

> [示例项目说明 →](../examples/README.md)

所有示例项目均为独立可运行的 Spring Boot 项目，配套 Docker Compose 文件用于启动依赖中间件。

```
examples/
├── spring-core-demo/               # IoC、Bean 生命周期、条件装配
├── spring-mvc-demo/                # RESTful CRUD、参数校验、统一异常处理
├── spring-boot-starter-demo/       # 自定义 Starter、Profile
├── spring-transaction-demo/        # 事务传播行为、事务失效场景
├── spring-mysql-demo/              # MySQL + JPA + MyBatis
├── spring-redis-demo/              # Redis 缓存
├── spring-mongodb-demo/            # MongoDB 文档存储
├── spring-es-demo/                 # Elasticsearch 搜索
├── spring-clickhouse-demo/         # ClickHouse 分析
├── spring-influxdb-demo/           # InfluxDB 时序数据
├── spring-security-demo/           # Security + JWT
├── spring-jpa-advanced-demo/       # JPA 动态查询、审计、Flyway
├── spring-cache-demo/              # Caffeine + Redis 多级缓存
├── spring-scheduling-demo/         # 定时任务、Quartz
├── spring-async-demo/              # 异步处理、线程池、事件驱动
├── spring-websocket-demo/          # WebSocket 聊天、SSE 推送
├── spring-batch-demo/              # 批处理 ETL
├── spring-webflux-demo/            # 响应式 Web + R2DBC
├── spring-modulith-demo/           # 模块化单体
├── spring-microservice-demo/       # 微服务综合示例（多模块）
│   ├── gateway-service/
│   ├── user-service/
│   ├── order-service/
│   └── common/
└── docker-compose/                 # 中间件 Docker Compose 文件
```

---

## 学习路线建议 / Learning Path

```
                    ┌─────────────────────────┐
                    │   〇、核心基础篇          │
                    │  IoC → MVC → AutoConfig  │
                    │  → Transaction           │
                    └──────────┬──────────────┘
                               │
                    ┌──────────▼──────────────┐
                    │   Spring Boot 基础        │
                    │  (Actuator, Logging)      │
                    └──────────┬──────────────┘
                               │
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
     ┌────────────┐   ┌──────────────┐   ┌──────────┐
     │  数据库篇   │   │  框架核心篇   │   │ 安全认证  │
     │ MySQL/Redis │   │ AOP/Testing  │   │ Security │
     │             │   │ Cache/Async  │   │          │
     └──────┬─────┘   └──────┬───────┘   └────┬─────┘
            │                │                 │
            └────────────────┼─────────────────┘
                             ▼
                    ┌─────────────────────┐
                    │     微服务篇         │
                    │ Gateway → Discovery │
                    │ → Feign → 熔断 → MQ │
                    └──────────┬──────────┘
                               ▼
              ┌────────────────┼────────────────┐
              ▼                                ▼
     ┌─────────────────┐              ┌──────────────────┐
     │   高级数据库      │              │   进阶主题篇      │
     │ ES/ClickHouse    │              │ WebFlux/Batch    │
     │ /InfluxDB        │              │ WebSocket/Modulith│
     └─────────────────┘              └──────────────────┘
```

### 推荐顺序 / Recommended Order

1. **核心基础**：IoC & DI → Spring MVC → 自动配置与 Starter → 事务管理
2. **入门**：Actuator → Logging → AOP → Testing
3. **数据层**：MySQL (JPA) → MySQL (MyBatis) → Redis → MongoDB
4. **安全**：Spring Security + JWT
5. **框架进阶**：JPA 深入 → 缓存体系 → 任务调度 → 异步处理 → 数据校验 → 文件上传/下载
6. **微服务**：Gateway → Service Discovery → Config Center → OpenFeign → Circuit Breaker → MQ → OpenTelemetry → Seata
7. **进阶数据库**：Elasticsearch → ClickHouse → InfluxDB
8. **进阶主题**：WebSocket/SSE → Spring Batch → WebFlux → Spring Modulith → Docker 部署
9. **API 文档**：OpenAPI / Swagger

---

## 环境准备 / Prerequisites

```bash
# Java 21+
java -version

# Maven 3.9+
mvn -version

# Docker & Docker Compose (用于启动中间件)
docker --version
docker compose version
```

## 快速启动所有中间件 / Quick Start All Middleware

```bash
cd examples/docker-compose
docker compose -f full-stack-compose.yml up -d
```
