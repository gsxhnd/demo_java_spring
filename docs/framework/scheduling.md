# 任务调度 / Task Scheduling

> 定时任务是业务系统的常见需求。从简单的 `@Scheduled` 到分布式调度平台 XXL-Job，选择合适的方案至关重要。

## 1. 概述 / Overview

Spring 提供了内置的任务调度支持（`@Scheduled`），适合单机简单场景。当应用部署多实例时，需要引入分布式调度框架（Quartz、XXL-Job、PowerJob）来避免任务重复执行，并提供可视化管理、失败重试、分片等能力。

## 2. 核心概念 / Core Concepts

### 2.1 调度方案对比

| 特性 | @Scheduled | Quartz | XXL-Job | PowerJob |
|------|-----------|--------|---------|----------|
| 复杂度 | 极低 | 中 | 低 | 低 |
| 分布式支持 | 不支持 | 支持（DB 锁） | 支持（调度中心） | 支持 |
| 可视化管理 | 无 | 无（需自建） | Web 控制台 | Web 控制台 |
| 动态修改 Cron | 不支持 | 支持 | 支持 | 支持 |
| 失败重试 | 不支持 | 支持 | 支持 | 支持 |
| 任务分片 | 不支持 | 不支持 | 支持 | 支持 |
| 工作流编排 | 不支持 | 不支持 | 支持（子任务） | 支持（DAG） |
| 适用场景 | 单机简单定时 | 中等复杂度 | 主流分布式调度 | 大规模任务调度 |

### 2.2 @Scheduled 触发方式

| 属性 | 说明 | 示例 |
|------|------|------|
| `fixedRate` | 固定频率（ms），上次开始后计时 | `@Scheduled(fixedRate = 5000)` |
| `fixedDelay` | 固定延迟（ms），上次结束后计时 | `@Scheduled(fixedDelay = 5000)` |
| `cron` | Cron 表达式 | `@Scheduled(cron = "0 0 2 * * ?")` |
| `initialDelay` | 首次延迟执行（ms） | 配合 fixedRate/fixedDelay |
| `zone` | 时区 | `@Scheduled(cron = "...", zone = "Asia/Shanghai")` |

### 2.3 Cron 表达式速查

```
┌──────── 秒 (0-59)
│ ┌────── 分 (0-59)
│ │ ┌──── 时 (0-23)
│ │ │ ┌── 日 (1-31)
│ │ │ │ ┌ 月 (1-12)
│ │ │ │ │ ┌ 周 (0-7, 0和7都是周日)
│ │ │ │ │ │
* * * * * *
```

| 表达式 | 含义 |
|--------|------|
| `0 0 2 * * ?` | 每天凌晨 2:00 |
| `0 */5 * * * ?` | 每 5 分钟 |
| `0 0 9-18 * * MON-FRI` | 工作日 9:00-18:00 每小时 |
| `0 0 0 1 * ?` | 每月 1 号零点 |
| `0 0 0 L * ?` | 每月最后一天零点 |

> 注意：Spring Cron 是 6 位（含秒），Linux crontab 是 5 位（不含秒）。

### 2.4 Quartz 架构

```
┌─────────────────────────────────────────┐
│              Scheduler                   │
│                                          │
│  ┌──────────┐     ┌──────────────────┐  │
│  │  Trigger  │────▶│      Job         │  │
│  │ (何时执行) │     │  (执行什么)       │  │
│  └──────────┘     └──────────────────┘  │
│                                          │
│  ┌──────────────────────────────────┐   │
│  │         JobStore                  │   │
│  │  RAMJobStore (单机)               │   │
│  │  JDBCJobStore (集群，DB 行锁)     │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

### 2.5 XXL-Job 架构

```
┌──────────────────┐         ┌──────────────────┐
│  XXL-Job Admin    │◄───────▶│  MySQL (调度数据)  │
│  (调度中心)        │         └──────────────────┘
│  - 任务管理        │
│  - 调度触发        │
│  - 执行日志        │
└────────┬─────────┘
         │ HTTP 调度
    ┌────┴────┐
    ▼         ▼
┌────────┐ ┌────────┐
│Executor│ │Executor│    ← 执行器（嵌入应用中）
│ App-1  │ │ App-2  │
└────────┘ └────────┘
```

**XXL-Job 路由策略：**

| 策略 | 说明 |
|------|------|
| FIRST | 固定第一个执行器 |
| ROUND | 轮询 |
| RANDOM | 随机 |
| CONSISTENT_HASH | 一致性哈希 |
| LEAST_FREQUENTLY_USED | 最不经常使用 |
| FAILOVER | 故障转移 |
| SHARDING_BROADCAST | 分片广播（所有执行器都执行，各自处理不同分片） |

## 3. 快速集成 / Quick Start

### 3.1 Maven 依赖

| 方案 | 依赖 |
|------|------|
| @Scheduled | `spring-boot-starter`（内置） |
| Quartz | `spring-boot-starter-quartz` |
| XXL-Job | `com.xuxueli:xxl-job-core` |

### 3.2 关键配置项

**@Scheduled：**

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.task.scheduling.pool.size` | `1` | 调度线程池大小（建议调大） |
| `spring.task.scheduling.thread-name-prefix` | `scheduling-` | 线程名前缀 |

**Quartz：**

| 配置项 | 说明 |
|--------|------|
| `spring.quartz.job-store-type` | `memory` / `jdbc` |
| `spring.quartz.jdbc.initialize-schema` | `always` / `never` |
| `spring.quartz.properties.org.quartz.jobStore.isClustered` | 集群模式 |

**XXL-Job：**

| 配置项 | 说明 |
|--------|------|
| `xxl.job.admin.addresses` | 调度中心地址 |
| `xxl.job.executor.appname` | 执行器名称 |
| `xxl.job.executor.port` | 执行器端口 |

## 4. 进阶要点 / Advanced Topics

- **@Scheduled 线程池** — 默认单线程，多个定时任务会串行阻塞，务必配置 `pool.size`
- **ShedLock** — 轻量级分布式锁方案，让 `@Scheduled` 在集群中只执行一次
- **Quartz 持久化** — `JDBCJobStore` 通过数据库行锁实现集群调度，需要 11 张表
- **XXL-Job 分片** — 大数据量任务拆分到多个执行器并行处理
- **任务幂等性** — 无论调度方案，任务本身必须保证幂等（重复执行不产生副作用）
- **超时与重试** — 设置合理的任务超时时间和重试策略
- **任务监控告警** — 任务执行失败时发送告警通知

## 5. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 定时任务不执行 | 缺少 `@EnableScheduling` | 配置类加注解 |
| 多个任务串行阻塞 | 默认单线程 | 增大 `pool.size` |
| 集群重复执行 | @Scheduled 无分布式锁 | 使用 ShedLock 或切换到 XXL-Job |
| Cron 表达式不生效 | 6 位 vs 5 位混淆 | Spring 用 6 位（含秒） |
| Quartz 集群不同步 | 各节点时钟不一致 | NTP 时钟同步 |
| XXL-Job 执行器注册失败 | 网络不通或端口被占 | 检查防火墙和端口配置 |

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-scheduling-demo/`](../../examples/spring-scheduling-demo/)

**演示功能：**
- @Scheduled 定时任务
- Cron 表达式详解
- Quartz Job 配置
- 多线程调度池配置
- 手动触发任务
- 任务统计监控

**运行示例：**
```bash
cd examples/spring-scheduling-demo
mvn spring-boot:run
```

**API 接口：**
- `POST /api/scheduling/reports/daily` - 手动生成日报
- `POST /api/scheduling/cleanup/sessions` - 手动清理会话
- `GET /api/scheduling/health` - 系统健康检查
- `GET /api/scheduling/cron-help` - Cron 表达式帮助

## 7. 参考链接 / References

- [Spring Framework Reference — Task Scheduling](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- [Spring Boot Reference — Quartz](https://docs.spring.io/spring-boot/reference/io/quartz.html)
- [XXL-Job 官方文档](https://www.xuxueli.com/xxl-job/)
- [ShedLock GitHub](https://github.com/lukas-krecan/ShedLock)
