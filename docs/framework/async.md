# 异步处理与线程池 / Async Processing & Thread Pool

> 异步处理是提升系统吞吐量的关键手段。Spring 的 `@Async`、事件机制和 `CompletableFuture` 让异步编程变得简单，但线程池配置不当会带来严重问题。

## 1. 概述 / Overview

Spring 提供了多种异步处理机制：`@Async` 注解将方法异步执行、`ApplicationEvent` 实现事件驱动解耦、`CompletableFuture` 编排复杂异步流程。这些机制的底层都依赖线程池，合理配置线程池是异步编程的核心。

## 2. 核心概念 / Core Concepts

### 2.1 异步方案对比

| 方案 | 适用场景 | 返回值 | 复杂度 |
|------|---------|--------|--------|
| `@Async` | 简单异步执行 | `void` / `Future` / `CompletableFuture` | 低 |
| `ApplicationEvent` | 事件驱动解耦 | 无（发布-订阅） | 低 |
| `CompletableFuture` | 异步编排、组合 | `CompletableFuture<T>` | 中 |
| `TaskExecutor` | 直接提交任务 | `Future<T>` | 中 |
| 消息队列 | 跨服务异步 | 无（最终一致性） | 高 |

### 2.2 @Async 工作原理

```
调用方
  │
  ▼
AOP 代理拦截 @Async 方法
  │
  ▼
将方法调用封装为 Runnable/Callable
  │
  ▼
提交到 TaskExecutor (线程池)
  │
  ▼
线程池中的线程异步执行方法
  │
  ▼
返回 Future/CompletableFuture (如果有返回值)
```

### 2.3 线程池核心参数

| 参数 | 说明 | 建议值 |
|------|------|--------|
| `corePoolSize` | 核心线程数（常驻） | CPU 密集型：N+1；IO 密集型：2N |
| `maxPoolSize` | 最大线程数 | 核心线程数的 2-4 倍 |
| `queueCapacity` | 等待队列容量 | 根据业务量评估，不宜过大 |
| `keepAliveSeconds` | 非核心线程空闲存活时间 | 60s |
| `threadNamePrefix` | 线程名前缀 | 便于日志排查 |
| `rejectedExecutionHandler` | 拒绝策略 | 见下表 |

**拒绝策略：**

| 策略 | 行为 | 适用场景 |
|------|------|---------|
| `AbortPolicy` | 抛出 RejectedExecutionException | 默认，快速失败 |
| `CallerRunsPolicy` | 调用方线程执行 | 不丢弃任务，但会阻塞调用方 |
| `DiscardPolicy` | 静默丢弃 | 可丢弃的任务 |
| `DiscardOldestPolicy` | 丢弃队列最老的任务 | 优先处理新任务 |

**线程池任务处理流程：**

```
提交任务
    │
    ▼
核心线程数未满？ ──Yes──▶ 创建核心线程执行
    │
    No
    ▼
等待队列未满？ ──Yes──▶ 放入队列等待
    │
    No
    ▼
最大线程数未满？ ──Yes──▶ 创建非核心线程执行
    │
    No
    ▼
执行拒绝策略
```

### 2.4 Spring 事件机制

```
事件发布者                          事件监听者
    │                                  │
    ▼                                  │
ApplicationEventPublisher              │
    .publishEvent(event)               │
    │                                  │
    ▼                                  ▼
┌──────────────────────────────────────────┐
│        ApplicationEvent                   │
│  (自定义事件，继承 ApplicationEvent        │
│   或直接用任意对象作为事件)                 │
└──────────────────────────────────────────┘
    │
    ├──▶ @EventListener              ← 同步监听（默认）
    ├──▶ @Async + @EventListener     ← 异步监听
    └──▶ @TransactionalEventListener ← 事务提交后监听
```

**@TransactionalEventListener 阶段：**

| phase | 触发时机 |
|-------|---------|
| `AFTER_COMMIT` | 事务提交后（默认） |
| `AFTER_ROLLBACK` | 事务回滚后 |
| `AFTER_COMPLETION` | 事务完成后（无论提交或回滚） |
| `BEFORE_COMMIT` | 事务提交前 |

### 2.5 CompletableFuture 常用操作

| 方法 | 说明 | 类比 |
|------|------|------|
| `supplyAsync(supplier, executor)` | 异步执行有返回值的任务 | 起点 |
| `thenApply(fn)` | 同步转换结果 | map |
| `thenApplyAsync(fn, executor)` | 异步转换结果 | async map |
| `thenCompose(fn)` | 扁平化嵌套 Future | flatMap |
| `thenCombine(other, fn)` | 合并两个 Future 的结果 | zip |
| `allOf(cf1, cf2, ...)` | 等待所有完成 | Promise.all |
| `anyOf(cf1, cf2, ...)` | 任一完成即返回 | Promise.race |
| `exceptionally(fn)` | 异常处理 | catch |
| `handle(fn)` | 统一处理结果和异常 | finally |
| `orTimeout(timeout, unit)` | 超时控制（Java 9+） | timeout |

## 3. 快速集成 / Quick Start

### 3.1 Maven 依赖

| 依赖 | 说明 |
|------|------|
| `spring-boot-starter` | 内置 `@Async` 和事件支持 |

> 无需额外依赖，Spring Boot Starter 已包含所有异步能力。

### 3.2 关键配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.task.execution.pool.core-size` | `8` | 核心线程数 |
| `spring.task.execution.pool.max-size` | `Integer.MAX_VALUE` | 最大线程数（务必设置上限） |
| `spring.task.execution.pool.queue-capacity` | `Integer.MAX_VALUE` | 队列容量（务必设置上限） |
| `spring.task.execution.pool.keep-alive` | `60s` | 非核心线程存活时间 |
| `spring.task.execution.thread-name-prefix` | `task-` | 线程名前缀 |
| `spring.task.execution.shutdown.await-termination` | `false` | 优雅关闭时等待任务完成 |
| `spring.task.execution.shutdown.await-termination-period` | — | 等待超时时间 |

## 4. 进阶要点 / Advanced Topics

- **多线程池隔离** — 不同业务使用不同线程池，避免慢任务拖垮快任务
- **MDC 跨线程传递** — 自定义 `TaskDecorator` 将 traceId 等 MDC 上下文传递到子线程
- **SecurityContext 传递** — `DelegatingSecurityContextExecutor` 传递安全上下文
- **虚拟线程（Java 21）** — Spring Boot 4.0 默认在 Java 21+ 环境下启用虚拟线程，适合 IO 密集型
- **线程池监控** — 通过 Micrometer 暴露线程池指标（活跃线程数、队列大小、拒绝次数）
- **优雅关闭** — 配置 `await-termination` 确保应用关闭时等待异步任务完成
- **@Async 异常处理** — 实现 `AsyncUncaughtExceptionHandler` 处理无返回值方法的异常
- **`TaskExecutorAutoConfiguration`** — Spring Boot 自动配置 `ThreadPoolTaskExecutor`，可通过 `spring.task.execution.*` 调优

## 5. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| `@Async` 不生效 | 自调用 / 缺少 `@EnableAsync` | 同 AOP 代理限制 |
| 线程池队列堆积 OOM | 队列无界 + 任务积压 | 设置 `queue-capacity` 上限 |
| 异步方法异常丢失 | void 返回值的异常无人处理 | 实现 `AsyncUncaughtExceptionHandler` |
| MDC traceId 丢失 | 子线程无 MDC 上下文 | 自定义 `TaskDecorator` |
| 事件监听器中事务不生效 | `@Async` 监听器在新线程，无事务上下文 | 监听器方法加 `@Transactional` |
| 应用关闭时任务丢失 | 未配置优雅关闭 | 设置 `await-termination=true` |

## 6. 示例项目 / Example

> 示例项目位于 [`examples/spring-async-demo/`](../../examples/spring-async-demo/)（待创建）
>
> 将演示：@Async 异步方法、自定义线程池、CompletableFuture 编排、Spring Event 事件驱动、MDC 传递

## 7. 参考链接 / References

- [Spring Framework Reference — Task Execution](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- [Spring Boot Reference — Task Execution](https://docs.spring.io/spring-boot/reference/features/task-execution-and-scheduling.html)
- [Baeldung — Spring Async](https://www.baeldung.com/spring-async)
- [Baeldung — Spring Events](https://www.baeldung.com/spring-events)
- [Java CompletableFuture Guide](https://www.baeldung.com/java-completablefuture)
