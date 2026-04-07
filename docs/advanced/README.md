# 进阶主题 / Advanced Topics

> 本章涵盖 Spring 生态中的进阶技术，适合在掌握核心基础和常用框架能力后深入学习。

## 版本基准 / Version Baseline

| 组件 | 版本 |
|------|------|
| Java | 21 (LTS) |
| Spring Framework | 7.0.6 |
| Spring Boot | 4.0.5 |

## 文档索引 / Document Index

| 文档 | 主题 | 关键词 |
|------|------|--------|
| [WebSocket / SSE 实时通信](websocket.md) | WebSocket、STOMP、Server-Sent Events | `@MessageMapping` `SimpMessagingTemplate` `SseEmitter` |
| [Spring Batch 批处理](batch.md) | 批处理框架、ETL、数据迁移 | `Job` `Step` `ItemReader` `ItemWriter` `Chunk` |
| [Spring WebFlux 响应式编程](webflux.md) | Reactor、Mono/Flux、R2DBC | `@RestController` `WebClient` `RouterFunction` `R2dbcRepository` |
| [Spring Modulith 模块化单体](modulith.md) | 模块化架构、事件驱动、模块边界 | `@ApplicationModule` `ApplicationModuleTest` `EventExternalization` |
| [Docker 部署 Java 应用](docker-deploy.md) | 容器化部署、镜像构建、JVM 调优 | `Dockerfile` `多阶段构建` `docker-compose` `JVM 容器参数` |

## 学习建议 / Recommendations

```
掌握 Core + Framework + Database + Microservice 后
    │
    ├──▶ WebSocket / SSE    ← 需要实时推送功能时
    ├──▶ Spring Batch       ← 需要批量数据处理时
    ├──▶ Spring WebFlux     ← 需要高并发 IO 密集型场景时
    ├──▶ Spring Modulith    ← 重新审视架构选型时
    └──▶ Docker 部署        ← 需要容器化部署时
```

这些主题相对独立，可以根据项目需求选择性学习，不需要按顺序。

## 与其他章节的关系 / Relations

| 进阶主题 | 前置知识 | 关联文档 |
|---------|---------|---------|
| WebSocket | Spring MVC、Security | [Spring MVC](../core/spring-mvc.md)、[Security](../framework/security-jwt.md) |
| Spring Batch | 事务管理、数据库 | [事务管理](../core/transaction.md)、[MySQL](../database/mysql.md) |
| WebFlux | IoC、Spring MVC（对比） | [IoC](../core/ioc-di.md)、[Gateway](../microservice/gateway.md) |
| Modulith | IoC、事件机制 | [IoC](../core/ioc-di.md)、[异步处理](../framework/async.md) |
| Docker 部署 | Spring Boot、Actuator | [Actuator](../framework/actuator.md)、[快速开始](../getting-started.md) |
