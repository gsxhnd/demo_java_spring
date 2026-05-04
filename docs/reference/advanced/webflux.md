# Spring WebFlux 响应式编程 / Reactive Programming

> 当系统面临高并发 IO 密集型场景（网关、聚合服务、流式数据处理）时，响应式编程模型能用更少的线程处理更多的请求。

## 1. 概述 / Overview

Spring WebFlux 是 Spring Framework 5 引入的响应式 Web 框架，基于 Project Reactor 实现。与 Spring MVC 的"一个请求一个线程"模型不同，WebFlux 采用非阻塞 IO + 事件循环模型，少量线程即可处理大量并发连接。Spring Cloud Gateway 就是基于 WebFlux 构建的。

## 2. 核心概念 / Core Concepts

### 2.1 Spring MVC vs WebFlux

| 特性 | Spring MVC | Spring WebFlux |
|------|-----------|----------------|
| 编程模型 | 命令式（Imperative） | 响应式（Reactive） |
| 线程模型 | 一个请求一个线程 | 事件循环（Event Loop） |
| 服务器 | Servlet 容器（Tomcat） | Netty / Undertow / Servlet 3.1+ |
| 阻塞 IO | 支持 | 不支持（全链路非阻塞） |
| 数据库访问 | JDBC / JPA | R2DBC / Reactive MongoDB / Reactive Redis |
| 适用场景 | 通用 Web 应用 | 高并发 IO 密集型、流式处理、网关 |
| 学习曲线 | 低 | 高（Reactor 操作符） |
| 调试难度 | 低 | 高（异步堆栈） |

### 2.2 Reactor 核心类型

```
Mono<T>  ── 0 或 1 个元素的异步序列
    │
    │  类比：CompletableFuture<T>，但支持背压和惰性求值
    │
Flux<T>  ── 0 到 N 个元素的异步序列
    │
    │  类比：Stream<T>，但异步 + 背压
    │
    ▼
Subscriber ── 订阅后才开始执行（惰性）
```

### 2.3 常用 Reactor 操作符

| 类别 | 操作符 | 说明 |
|------|--------|------|
| 创建 | `Mono.just()` / `Flux.fromIterable()` | 从值创建 |
| 创建 | `Mono.empty()` / `Mono.error()` | 空/错误信号 |
| 转换 | `map` / `flatMap` | 同步/异步转换 |
| 过滤 | `filter` / `take` / `skip` | 过滤元素 |
| 组合 | `zip` / `merge` / `concat` | 合并多个流 |
| 错误处理 | `onErrorReturn` / `onErrorResume` / `retry` | 异常处理 |
| 背压 | `onBackpressureBuffer` / `onBackpressureDrop` | 背压策略 |
| 调度 | `subscribeOn` / `publishOn` | 切换执行线程 |
| 副作用 | `doOnNext` / `doOnError` / `doOnComplete` | 日志、监控 |

### 2.4 两种编程风格

| 风格 | 说明 | 适用场景 |
|------|------|---------|
| 注解式 | `@RestController` + `@GetMapping`，返回 `Mono`/`Flux` | 从 MVC 迁移，团队熟悉注解 |
| 函数式 | `RouterFunction` + `HandlerFunction` | 轻量级路由，Lambda 风格 |

### 2.5 WebClient（响应式 HTTP 客户端）

| 特性 | RestTemplate | WebClient |
|------|-------------|-----------|
| 阻塞/非阻塞 | 阻塞 | 非阻塞 |
| 响应式支持 | 不支持 | 原生支持 |
| 流式响应 | 不支持 | 支持 |
| 状态 | 维护模式（不再新增功能） | 推荐使用 |

> Spring Framework 7.x 引入了 `RestClient`（同步但现代化的 API），作为 `RestTemplate` 的替代。

### 2.6 R2DBC（响应式数据库访问）

```
传统 JDBC:  Thread ──block──▶ DB ──block──▶ Thread 继续
R2DBC:      Thread ──async──▶ DB ──callback──▶ 任意 Thread 继续
```

| 特性 | JDBC | R2DBC |
|------|------|-------|
| 阻塞 | 是 | 否 |
| 连接池 | HikariCP | r2dbc-pool |
| ORM | JPA / Hibernate | Spring Data R2DBC（无懒加载） |
| 支持数据库 | 所有 | PostgreSQL, MySQL, H2, MSSQL, Oracle |
| 事务 | `@Transactional` | `@Transactional` (ReactiveTransactionManager) |

## 3. 快速集成 / Quick Start

### 3.1 Maven 依赖

| 依赖 | 说明 |
|------|------|
| `spring-boot-starter-webflux` | WebFlux + Netty |
| `spring-boot-starter-data-r2dbc` | R2DBC 响应式数据库 |
| `io.asyncer:r2dbc-mysql` | MySQL R2DBC 驱动 |
| `org.postgresql:r2dbc-postgresql` | PostgreSQL R2DBC 驱动 |
| `spring-boot-starter-data-mongodb-reactive` | 响应式 MongoDB |
| `spring-boot-starter-data-redis-reactive` | 响应式 Redis |

### 3.2 关键配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.r2dbc.url` | — | R2DBC 连接 URL（`r2dbc:mysql://...`） |
| `spring.r2dbc.username` | — | 数据库用户名 |
| `spring.r2dbc.password` | — | 数据库密码 |
| `spring.r2dbc.pool.initial-size` | `10` | 连接池初始大小 |
| `spring.r2dbc.pool.max-size` | `10` | 连接池最大大小 |

## 4. 进阶要点 / Advanced Topics

- **全链路非阻塞** — WebFlux 的性能优势要求全链路非阻塞，任何阻塞调用（JDBC、同步 HTTP）都会破坏模型
- **背压（Backpressure）** — Reactor 的核心优势，消费者控制生产者速率，防止内存溢出
- **Context 传播** — Reactor Context 替代 ThreadLocal（MDC、SecurityContext），Spring Boot 4.x 通过 Micrometer Context Propagation 简化
- **测试** — `StepVerifier` 验证 Mono/Flux 的元素、错误和完成信号
- **调试** — `Hooks.onOperatorDebug()` 或 `reactor-tools` 增强异步堆栈信息
- **虚拟线程 vs WebFlux** — Java 21 虚拟线程让 MVC 也能高并发，WebFlux 的优势缩小但在流式处理场景仍有价值
- **混合使用** — 同一应用不能同时用 MVC 和 WebFlux，但可以在 MVC 应用中使用 `WebClient`

## 5. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 返回 Mono 但没有执行 | Reactor 是惰性的，没有订阅 | 确保返回给框架（Controller 返回值会自动订阅） |
| 阻塞调用导致性能下降 | 在 Reactor 线程上执行阻塞操作 | 用 `Schedulers.boundedElastic()` 隔离阻塞调用 |
| ThreadLocal 丢失 | Reactor 线程切换 | 使用 Reactor Context + Context Propagation |
| R2DBC 不支持关联查询 | R2DBC 无 ORM 懒加载 | 手动 JOIN 查询或多次查询组合 |
| 调试困难 | 异步堆栈不连续 | 启用 `Hooks.onOperatorDebug()` |

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-webflux-demo/`](../../examples/spring-webflux-demo/)

**演示功能：**
- WebFlux REST API
- R2DBC 响应式数据库
- Mono/Flux 响应式流
- SSE 实时推送
- Flux 聚合查询

**运行示例：**
```bash
cd examples/spring-webflux-demo
mvn spring-boot:run

# 查询产品
curl http://localhost:8080/api/products

# SSE 流
curl -N http://localhost:8080/api/products/sse/time
```

## 7. 参考链接 / References

- [Spring WebFlux Reference](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor Reference](https://projectreactor.io/docs/core/release/reference/)
- [Spring Data R2DBC Reference](https://docs.spring.io/spring-data/relational/reference/)
- [Baeldung — Spring WebFlux](https://www.baeldung.com/spring-webflux)
