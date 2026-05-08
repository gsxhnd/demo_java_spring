---
title: Java Spring 异步与定时任务
created: 2026-05-08 22:29:19
category: Java-Spring
tags:
  - Java
  - Spring
  - Async
  - Scheduled
  - 异步处理
  - 定时任务
---

<!-- markdownlint-disable MD025 -->

# Java Spring 异步与定时任务

## 为什么要学异步与定时任务

前面几节我们构建了一个完整的请求-响应链路：Controller 接收请求 → Service 处理业务 → Repository 持久化数据。这个链路是**同步**的 — 用户发一个请求，线程一直等到 Service 处理完才返回响应。但当业务中有耗时操作（发送邮件、生成报表、调用第三方 API）时，让用户等在页面上转圈就不是好设计了。另外，有些操作根本不需要用户触发 — 比如每天凌晨统计前一天的订单数据、每小时清理过期缓存。

异步处理解决"不用等"的问题，定时任务解决"到点就做"的问题。它们是业务能力的重要组成部分。

## 核心概念

### @Async 是什么

`@Async` 是 Spring 提供的声明式异步方法执行注解。标注在方法上，调用该方法时会提交给线程池异步执行，调用方立即返回而不等待方法执行完毕。

**换个说法：** 同步就像你去柜台点餐，站那等着直到餐做好才走。异步就像你扫码点餐取了号，回去坐着等叫号 — 你不用堵在柜台前，但餐还是会做好。`@Async` 就是这个"扫码取号"的操作。

### 为什么需要 @Async

**痛点场景：** 用户注册后需要发送"欢迎邮件"。如果同步发送，注册接口的响应时间 = 写数据库时间 + SMTP 连接时间 + 邮件发送时间。万一 SMTP 服务器响应慢（比如 3 秒），用户的注册请求就要等 3 秒才能看到成功提示。在流量高峰期，这 3 秒可能把线程池耗光。

**设计动机：** 将非关键路径的操作从主流程中剥离，异步执行。用户注册的主流程只需要写数据库（毫秒级），发送邮件可以后台慢慢做。即使邮件发送失败，也不影响用户看到"注册成功"。

### 没有 @Async 会怎样

**困境：** 要用 Java 原生方式实现异步，你需要手动创建 `ExecutorService`、管理线程池、处理异常。要么写大量样板代码，要么自己封装一个异步框架。

**有了 @Async 之后：** 一个注解搞定。Spring 自动将方法调用包装为异步任务，提交到你配置的线程池。返回值可以用 `Future`、`CompletableFuture` 或 `ListenableFuture` 获取。

### @Scheduled 是什么

`@Scheduled` 是 Spring 提供的声明式定时任务注解。标注在方法上，Spring 会根据指定的 cron 表达式或固定速率周期性地执行该方法。

**换个说法：** 定时任务就像设置闹钟 — `@Scheduled` 告诉 Spring "每天早上 8 点叫醒这个方法执行一次"。

### 为什么需要 @Scheduled

**痛点场景：** 项目中有一堆需要在特定时间执行的任务 — 每天零点生成日报、每小时清理过期 Session、每 5 分钟同步一次外部数据。没有框架支持时，你需要引入 Quartz 之类的调度库，或者写一个 `ScheduledExecutorService` 自己算下次执行时间。

**设计动机：** Spring 用最简单的方式覆盖 80% 的定时任务场景。`@Scheduled` 能做到 cron 表达式、固定延迟、固定速率三种模式，对大多数应用足够了。

### 没有 @Scheduled 会怎样

**困境：** 用 Quartz 需要学一套新的 Job/Trigger API，配置繁琐。用 Linux cron 则把业务逻辑和部署环境耦合（在 Windows 开发环境中不存在 cron）。

**有了 @Scheduled 之后：** 一行注解定义定时规则，方法本身写业务逻辑。不依赖操作系统，不引入额外框架。

## 概念深入解释

### @Async 工作机制

**启用异步：** 在配置类或启动类上添加 `@EnableAsync`。

```java
@Configuration
@EnableAsync
public class AsyncConfig {
}
```

**使用异步：**

```java
@Service
public class NotificationService {

    @Async
    public CompletableFuture<Boolean> sendWelcomeEmail(String email) {
        // 耗时操作：连接 SMTP、发送邮件
        return CompletableFuture.completedFuture(true);
    }
}
```

**关键机制：** Spring 通过 AOP 代理拦截 `@Async` 方法调用。当你调用 `notificationService.sendWelcomeEmail(email)` 时，代理对象把调用提交给一个 `TaskExecutor` 线程池执行，然后立即返回。

**注意事项：**

- `@Async` 方法的返回值必须是 `void` 或 `Future`/`CompletableFuture`/`ListenableFuture`。如果你想拿到执行结果（或异常），用 `CompletableFuture`。
- 同一类内的 `@Async` 方法互调不会走代理，因此不会异步执行。这是 Spring AOP 代理机制的固有限制 — 内部调用绕过了代理。解决方法是把异步方法抽到另一个 Bean 中。

**自定义线程池：**

```java
@Bean("taskExecutor")
public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("async-");
    executor.initialize();
    return executor;
}
```

默认线程池是 `SimpleAsyncTaskExecutor`，它**不为每个任务创建新线程**（名字有误导性），但可能存在其他限制。生产环境建议显式配置一个线程池，并配合 `@Async("taskExecutor")` 指定使用哪个池。

### @Scheduled 工作机制

**启用定时任务：** 在配置类或启动类上添加 `@EnableScheduling`。

**使用方式：**

```java
@Component
public class ReportScheduler {

    @Scheduled(cron = "0 0 2 * * ?")   // 每天凌晨 2 点
    public void generateDailyReport() {
        // 生成报表逻辑
    }

    @Scheduled(fixedRate = 300000)      // 每 5 分钟
    public void syncExternalData() {
        // 同步外部数据
    }

    @Scheduled(fixedDelay = 60000)      // 上次结束后 60 秒再执行
    public void cleanExpiredCache() {
        // 清理过期缓存
    }
}
```

**三种调度模式：**

| 模式 | 含义 | 适用场景 |
|------|------|----------|
| `cron` | 按 cron 表达式定时触发 | 日报（每天 2 点）、周报（周一 9 点） |
| `fixedRate` | 固定速率（从任务开始计时） | 每 5 分钟拉取一次数据，不管上次是否完成 |
| `fixedDelay` | 固定延迟（从任务结束计时） | 上次清理完成后再等 60 秒开始下一次 |

**Cron 表达式格式（Spring 使用 6 位）：**
```
秒 分 时 日 月 星期
0  0  2  *  *  ?     → 每天凌晨 2 点
0  0  9  *  *  MON   → 每周一上午 9 点
0  */5 *  *  *  ?     → 每 5 分钟
```

### Async + Scheduled 的组合使用

定时任务方法本身也可以标注 `@Async`，让定时任务的执行不阻塞 Scheduler 线程：

```java
@Async
@Scheduled(fixedRate = 60000)
public void heavyTask() {
    // 如果这个任务执行需要 3 分钟，没有 @Async 会阻塞后续调度
}
```

### 分布式环境下的定时任务

Spring 的单机定时任务在分布式环境下有一个严重问题：如果部署了 3 个实例，同一个定时任务会在 3 台机器上同时触发。解决方式：
- 引入分布式锁（如 Redis `SETNX` 或 ShedLock）
- 使用分布式调度框架（如 XXL-JOB、Elastic-Job）
- 将定时任务抽取为独立调度服务（单实例部署）

## 核心要点

1. **异步化非关键路径操作：** 发送邮件、通知、日志写入等不需要同步等待的操作用 `@Async` 分离。
2. **生产环境配置自定义线程池：** 不要依赖默认线程池，显式配置 `corePoolSize`、`maxPoolSize`、`queueCapacity`。
3. **`@Scheduled` 方法应该是幂等的：** 如果任务失败或实例重启导致重复执行，结果应该一致。
4. **Cron 表达式注意时区：** 默认使用服务器时区，多时区部署时需显式设置 `zone` 属性。
5. **`fixedDelay` 比 `fixedRate` 更安全：** 当任务执行时间不稳定时，`fixedDelay` 不会造成任务堆叠。

## 常见误区

- **默认线程池在生产环境下表现很差。** `@Async` 默认使用的 `SimpleAsyncTaskExecutor`，其行为不适合生产环境。必须显式配置一个带队列容量的 `ThreadPoolTaskExecutor`，否则任务会无限堆积。
- **同一个类内调用 @Async 方法不会异步执行。** 这是 Spring AOP 代理的本质限制。`this.doAsync()` 在内部调用时直接执行，不经过代理，不会提交给线程池。解决方法是把异步方法独立到另一个 Bean。
- **`fixedRate` 会导致任务堆积。** 如果 `fixedRate=5000`（每 5 秒触发），但任务执行需要 10 秒，下一次触发会累积。应该在方法内加入并发控制（如分布式锁），或改用 `fixedDelay`。
- **忘记 @EnableAsync / @EnableScheduling 导致注解静默失效。** 没有这两个启用注解，`@Async` 和 `@Scheduled` 都会被忽略，且没有报错。排查时检查启动类或配置类上是否添加了启用注解。
- **`@Scheduled` 方法内部抛异常后任务终止。** 如果定时任务方法抛出未捕获异常，Spring 默认行为是任务终止，后续不再执行。应使用 `try-catch` 包裹业务逻辑，确保一次失败不影响后续调度。
- **在 @Async 方法返回值上用 void 而不是 CompletableFuture。** 用 `void` 时调用方无法知道任务是否完成、是否抛了异常。如果需要感知执行结果或做降级处理，用 `CompletableFuture`。

## 与其他概念的关联

- **前置：** [Java Spring AOP](./08_Java%20Spring%20AOP.md) -- `@Async` 通过 AOP 代理实现异步拦截
- **前置：** [Java Spring 容器](./09_Java%20Spring%20容器.md) -- 线程池作为 Bean 被 Spring 容器管理
- **前置：** [Java Spring Bean](./06_Java%20Spring%20Bean.md) -- 内部调用绕过代理是 Spring Bean 代理机制决定的
- **并行：** [Java Spring Service 层](./30_Java%20Spring%20Service%20层.md) -- `@Async` 和 `@Scheduled` 通常标注在 Service 方法上
- **后续：** [Java Spring Cloud 消息队列](../Spring_Cloud/Java Spring Cloud 消息队列.md) -- 复杂异步场景用消息队列替代 `@Async` 更可靠
- **后续：** [Java Spring Cloud 熔断限流](../Spring_Cloud/Java Spring Cloud 熔断限流.md) -- 异步调用下游服务时需要熔断保护
