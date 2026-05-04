# 进阶主题篇

> 高级 Spring 特性：实时通信、批处理、响应式编程、模块化单体、容器化部署。

## 为什么需要这篇

这些主题并非每个项目都会用到，但在特定场景下是不可替代的解决方案。将它们独立为进阶模块，放在学习路径的最后阶段，避免初学者在学习核心内容时被高级主题干扰。

## 版本基准

| 组件 | 版本 |
|------|------|
| Java | 21 (LTS) |
| Spring Framework | 7.0.6 |
| Spring Boot | 4.0.5 |

## 文档索引

| 文档 | 主题 | 关键词 |
|------|------|--------|
| [WebSocket / SSE 实时通信](01-websocket.md) | WebSocket、STOMP、Server-Sent Events | `@MessageMapping` `SimpMessagingTemplate` `SseEmitter` |
| [Spring Batch 批处理](02-batch.md) | 批处理框架、ETL、数据迁移 | `Job` `Step` `ItemReader` `ItemWriter` `Chunk` |
| [Spring WebFlux 响应式编程](03-webflux.md) | Reactor、Mono/Flux、R2DBC | `WebClient` `RouterFunction` `R2dbcRepository` |
| [Spring Modulith 模块化单体](04-modulith.md) | 模块化架构、事件驱动、模块边界 | `@ApplicationModule` `ApplicationModuleTest` `EventExternalization` |
| [Docker 部署 Java 应用](05-docker-deploy.md) | 容器化部署、镜像构建、JVM 调优 | `Dockerfile` `多阶段构建` `docker-compose` `JVM 容器参数` |

## 示例项目

| 示例项目 | 演示内容 |
|---------|---------|
| `examples/spring-websocket-demo/` | WebSocket + STOMP 聊天室、SSE 实时推送 |
| `examples/spring-batch-demo/` | CSV 导入、Chunk 处理、Skip/Retry 容错 |
| `examples/spring-webflux-demo/` | WebFlux + R2DBC、Mono/Flux、SSE |
| `examples/spring-modulith-demo/` | 模块化结构、事件驱动、Event Publication Log |

## 学习建议

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

## 与其他篇章的关系

| 进阶主题 | 前置知识 | 关联文档 |
|---------|---------|---------|
| WebSocket | Spring MVC、Security | [Spring MVC](../01-core/02-spring-mvc.md)、[Security](../03-framework/05-security-jwt.md) |
| Spring Batch | 事务管理、数据库 | [事务管理](../01-core/04-transaction.md)、[MySQL](../02-database/01-mysql.md) |
| WebFlux | IoC、Spring MVC（对比） | [IoC](../01-core/01-ioc-di.md)、[Gateway](../04-microservice/01-gateway.md) |
| Modulith | IoC、事件机制 | [IoC](../01-core/01-ioc-di.md)、[异步处理](../03-framework/11-async.md) |
| Docker 部署 | Spring Boot、Actuator | [Actuator](../03-framework/01-actuator.md) |

## 注意事项

- WebFlux 的响应式编程模型与传统 Servlet 模型差异巨大，需要从"思维方式"层面理解
- WebFlux 与传统 MVC 不能混用
- Spring Batch 的 Job 重启、跳过策略、事务边界是生产环境重点
- WebSocket 的连接管理、心跳机制、断线重连需要在示例中体现
- Modulith 是 Spring 较新的模块化方案，适合"先单体后拆分"的演进策略
- Docker 多阶段构建可以有效减小镜像体积
