# Spring Batch 批处理 / Batch Processing

> 当需要处理百万级数据导入导出、ETL、报表生成、数据迁移时，Spring Batch 提供了成熟的批处理框架。

## 1. 概述 / Overview

Spring Batch 是一个轻量级的批处理框架，提供了可复用的组件来处理大量数据。它支持事务管理、chunk 处理、重试/跳过、作业调度、并行处理等企业级批处理能力。Spring Boot 通过 `spring-boot-starter-batch` 提供自动配置。

## 2. 核心概念 / Core Concepts

### 2.1 批处理架构

```
┌─────────────────────────────────────────────────────┐
│                    JobLauncher                        │
│                  (启动 Job)                           │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│                      Job                             │
│              (一个完整的批处理任务)                     │
│                                                      │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐        │
│  │  Step 1   │──▶│  Step 2   │──▶│  Step 3   │        │
│  └──────────┘   └──────────┘   └──────────┘        │
│                                                      │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│                  JobRepository                        │
│           (元数据存储：执行状态、参数)                   │
│           BATCH_JOB_INSTANCE                         │
│           BATCH_JOB_EXECUTION                        │
│           BATCH_STEP_EXECUTION                       │
└─────────────────────────────────────────────────────┘
```

### 2.2 Step 类型

| 类型 | 说明 | 适用场景 |
|------|------|---------|
| Chunk-oriented | 读-处理-写，按 chunk 提交事务 | 大数据量 ETL |
| Tasklet | 单步执行一个任务 | 文件清理、存储过程调用 |

### 2.3 Chunk 处理模型

```
┌──────────┐    ┌──────────────┐    ┌──────────┐
│ItemReader │──▶│ItemProcessor │──▶│ItemWriter │
│ (读取数据) │    │ (转换/过滤)   │    │ (写入数据) │
└──────────┘    └──────────────┘    └──────────┘
     │                                    │
     │◄──── chunk-size (如 100) ────────▶│
     │                                    │
     │  读 100 条 → 逐条处理 → 批量写入    │
     │  一个 chunk 一个事务                 │
```

### 2.4 内置 Reader / Writer

**ItemReader：**

| Reader | 数据源 |
|--------|--------|
| `FlatFileItemReader` | CSV / 定长文件 |
| `JsonItemReader` | JSON 文件 |
| `JdbcCursorItemReader` | JDBC 游标查询 |
| `JdbcPagingItemReader` | JDBC 分页查询 |
| `JpaPagingItemReader` | JPA 分页查询 |
| `MongoItemReader` | MongoDB |
| `KafkaItemReader` | Kafka |

**ItemWriter：**

| Writer | 目标 |
|--------|------|
| `FlatFileItemWriter` | CSV / 文本文件 |
| `JsonFileItemWriter` | JSON 文件 |
| `JdbcBatchItemWriter` | JDBC 批量写入 |
| `JpaItemWriter` | JPA 写入 |
| `MongoItemWriter` | MongoDB |
| `KafkaItemWriter` | Kafka |

### 2.5 Job 执行状态

```
STARTING → STARTED → COMPLETED
                  ↘ FAILED → (重启) → STARTED → COMPLETED
                  ↘ STOPPED → (重启) → STARTED → ...
                  ↘ ABANDONED (放弃)
```

| 状态 | 说明 |
|------|------|
| `COMPLETED` | 成功完成，不可重新执行（相同参数） |
| `FAILED` | 失败，可从断点重启 |
| `STOPPED` | 手动停止，可从断点重启 |
| `ABANDONED` | 放弃，不可重启 |

### 2.6 容错机制

| 机制 | 说明 | 配置 |
|------|------|------|
| Skip | 跳过异常记录，继续处理 | `faultTolerant().skip(Exception.class).skipLimit(10)` |
| Retry | 重试失败操作 | `faultTolerant().retry(Exception.class).retryLimit(3)` |
| Restart | 从上次失败的位置重启 Job | JobRepository 记录执行位置 |

## 3. 快速集成 / Quick Start

### 3.1 Maven 依赖

| 依赖 | 说明 |
|------|------|
| `spring-boot-starter-batch` | Spring Batch + 自动配置 |
| `spring-boot-starter-jdbc` | JDBC（JobRepository 需要数据源） |

### 3.2 关键配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.batch.jdbc.initialize-schema` | `embedded` | 初始化元数据表（生产用 `never` + Flyway） |
| `spring.batch.job.enabled` | `true` | 启动时自动执行 Job |
| `spring.batch.job.name` | — | 指定启动时执行的 Job 名称 |

## 4. 进阶要点 / Advanced Topics

- **并行处理** — `TaskExecutor` 多线程 Step / `Partitioning` 分区处理 / `AsyncItemProcessor`
- **远程分区** — 主节点分配分区，工作节点并行处理（适合集群）
- **Flow 编排** — 条件分支（`on("FAILED").to(stepB)`）、并行 Flow（`split`）
- **JobParameters** — 传递运行时参数，相同参数的 Job 不会重复执行
- **Listeners** — `JobExecutionListener` / `StepExecutionListener` / `ItemReadListener` 等
- **chunk-size 调优** — 太小事务开销大，太大内存压力大，通常 100-1000
- **Spring Batch 6.x 新特性** — 基于 Java 21、新的 `JobBuilder` / `StepBuilder` API、MongoDB JobRepository、Micrometer 观测集成

## 5. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| Job 不执行 | `spring.batch.job.enabled=false` | 设为 true 或手动触发 |
| 相同参数 Job 不重复执行 | COMPLETED 状态的 Job 不可重跑 | 添加时间戳参数或用 `RunIdIncrementer` |
| 元数据表不存在 | `initialize-schema=never` | 手动创建或改为 `always` |
| 大数据量 OOM | chunk-size 过大或 Reader 一次加载全部 | 使用 PagingItemReader + 合理 chunk-size |
| 重启后从头开始 | Step 不可重启 | 配置 `allowStartIfComplete(true)` |

## 6. 示例项目 / Example

> 示例项目位于 [`examples/spring-batch-demo/`](../../examples/spring-batch-demo/)（待创建）
>
> 将演示：CSV 导入数据库、数据库导出 CSV、chunk 处理、Skip/Retry 容错、多步骤 Job

## 7. 参考链接 / References

- [Spring Batch Reference](https://docs.spring.io/spring-batch/reference/)
- [Spring Boot Reference — Batch](https://docs.spring.io/spring-boot/reference/io/batch.html)
- [Baeldung — Spring Batch](https://www.baeldung.com/introduction-to-spring-batch)
