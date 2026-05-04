# Spring 事务管理 / Transaction Management

> 事务是数据一致性的保障。Spring 的声明式事务极大简化了事务管理，但 `@Transactional` 的"坑"也是面试和生产中的高频问题。

## 1. 概述 / Overview

Spring 通过 `PlatformTransactionManager` 抽象层统一管理事务，支持 JDBC、JPA、MyBatis、MongoDB 等多种数据访问技术。声明式事务（`@Transactional`）基于 AOP 代理实现，是最常用的方式。

## 2. 核心概念 / Core Concepts

### 2.1 事务管理器体系

```
PlatformTransactionManager (顶层接口)
    │
    ├── DataSourceTransactionManager     ← JDBC / MyBatis
    ├── JpaTransactionManager            ← Spring Data JPA
    ├── MongoTransactionManager          ← MongoDB
    └── ChainedTransactionManager        ← 多数据源链式事务（非 XA）

ReactiveTransactionManager (响应式)
    └── R2dbcTransactionManager          ← R2DBC
```

> Spring Boot 自动配置：引入 `spring-boot-starter-data-jpa` 自动注册 `JpaTransactionManager`，引入 `spring-boot-starter-jdbc` 自动注册 `DataSourceTransactionManager`。

### 2.2 声明式 vs 编程式事务

| 特性 | 声明式 (`@Transactional`) | 编程式 (`TransactionTemplate`) |
|------|--------------------------|-------------------------------|
| 侵入性 | 低，注解声明 | 高，代码中显式控制 |
| 灵活性 | 方法级粒度 | 代码块级粒度 |
| 适用场景 | 90% 的业务场景 | 需要精细控制的场景 |
| 实现原理 | AOP 代理 | 直接调用 TransactionManager |

### 2.3 @Transactional 属性详解

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `propagation` | `REQUIRED` | 传播行为 |
| `isolation` | `DEFAULT` | 隔离级别（使用数据库默认） |
| `timeout` | `-1` | 超时时间（秒），-1 表示不超时 |
| `readOnly` | `false` | 只读事务（优化提示） |
| `rollbackFor` | — | 指定触发回滚的异常类型 |
| `noRollbackFor` | — | 指定不触发回滚的异常类型 |
| `transactionManager` | — | 指定事务管理器（多数据源时） |

### 2.4 传播行为 / Propagation

| 传播行为 | 说明 | 典型场景 |
|---------|------|---------|
| `REQUIRED` | 有事务就加入，没有就新建 | 默认，绝大多数场景 |
| `REQUIRES_NEW` | 总是新建事务，挂起当前事务 | 操作日志（不受主事务回滚影响） |
| `NESTED` | 嵌套事务（Savepoint） | 批量处理中单条失败不影响整体 |
| `SUPPORTS` | 有事务就加入，没有就非事务执行 | 查询方法 |
| `NOT_SUPPORTED` | 非事务执行，挂起当前事务 | 调用不支持事务的外部系统 |
| `MANDATORY` | 必须在事务中调用，否则抛异常 | 强制要求调用方开启事务 |
| `NEVER` | 必须非事务执行，有事务就抛异常 | 确保不在事务中执行 |

**传播行为图解：**

```
ServiceA.methodA()  @Transactional(REQUIRED)
    │
    ├── REQUIRED:      methodB 加入 A 的事务 ──── 同一个事务
    ├── REQUIRES_NEW:  methodB 新建独立事务 ──── 两个独立事务
    ├── NESTED:        methodB 在 A 中创建 Savepoint ── A 回滚则 B 也回滚，B 回滚不影响 A
    └── NOT_SUPPORTED: methodB 挂起 A 的事务，非事务执行
```

### 2.5 隔离级别 / Isolation

| 隔离级别 | 脏读 | 不可重复读 | 幻读 | 说明 |
|---------|------|-----------|------|------|
| `READ_UNCOMMITTED` | 可能 | 可能 | 可能 | 最低隔离，几乎不用 |
| `READ_COMMITTED` | 防止 | 可能 | 可能 | PostgreSQL/Oracle 默认 |
| `REPEATABLE_READ` | 防止 | 防止 | 可能 | MySQL InnoDB 默认 |
| `SERIALIZABLE` | 防止 | 防止 | 防止 | 最高隔离，性能最差 |
| `DEFAULT` | — | — | — | 使用数据库默认级别 |

### 2.6 事务失效的 7 大场景

| # | 场景 | 原因 | 解决方案 |
|---|------|------|---------|
| 1 | 自调用（同类方法调用） | 未经过代理对象 | 注入自身 / `AopContext.currentProxy()` / 拆分到不同类 |
| 2 | 方法非 public | CGLIB 代理要求 public | 改为 public 方法 |
| 3 | 异常被 catch 吞掉 | 事务管理器感知不到异常 | 重新抛出或手动 `setRollbackOnly()` |
| 4 | 抛出 checked 异常 | 默认只回滚 RuntimeException | 设置 `rollbackFor = Exception.class` |
| 5 | 数据库引擎不支持事务 | MyISAM 不支持事务 | 使用 InnoDB |
| 6 | 未被 Spring 管理 | 对象不是 Spring Bean | 确保通过容器获取 |
| 7 | 多线程场景 | 子线程不在同一事务中 | 编程式事务或分布式事务 |

## 3. 快速集成 / Quick Start

### 3.1 Maven 依赖

| 依赖 | 说明 |
|------|------|
| `spring-boot-starter-data-jpa` | JPA + 事务管理器自动配置 |
| `spring-boot-starter-jdbc` | JDBC + DataSourceTransactionManager |

> 引入以上任一 Starter，`@Transactional` 即可直接使用，无需额外配置。

### 3.2 关键配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.jpa.open-in-view` | `true` | OSIV 模式（建议关闭） |
| `logging.level.org.springframework.transaction` | — | 设为 `TRACE` 可查看事务日志 |
| `logging.level.org.springframework.jdbc.datasource.DataSourceTransactionManager` | — | JDBC 事务详细日志 |

## 4. 进阶要点 / Advanced Topics

- **`readOnly = true` 优化** — JPA 关闭脏检查（Dirty Checking），JDBC 可能走从库，MySQL 优化查询计划
- **大事务拆分** — 避免在 `@Transactional` 方法中做 RPC、文件 IO 等耗时操作，缩短事务持有连接的时间
- **编程式事务** — `TransactionTemplate` 或 `TransactionOperator`（响应式），适合需要代码块级控制的场景
- **多数据源事务** — 每个数据源配独立的 `TransactionManager`，`@Transactional("orderTxManager")` 指定
- **分布式事务** — 跨服务/跨数据源场景，参考 [分布式事务文档](../microservice/distributed-transaction.md)
- **OSIV（Open Session In View）** — Spring Boot 默认开启，会延长数据库连接持有时间，建议生产关闭 `spring.jpa.open-in-view=false`
- **事务事件** — `@TransactionalEventListener` 在事务提交后触发事件，适合发送通知、更新缓存等

## 5. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 事务不回滚 | 抛出 checked 异常 | 加 `rollbackFor = Exception.class` |
| 自调用事务不生效 | 未走代理 | 拆分到不同 Service 类 |
| `@Transactional` 在 private 方法上 | 代理无法拦截 | 改为 public |
| 长事务导致连接池耗尽 | 事务方法中有慢操作 | 拆分事务，缩小事务范围 |
| 并发更新丢失 | 缺少乐观锁/悲观锁 | JPA `@Version` 乐观锁或 `SELECT FOR UPDATE` |
| 多数据源事务混乱 | 使用了错误的事务管理器 | 显式指定 `transactionManager` |

## 6. 示例项目 / Example

> 示例项目位于 [`examples/spring-transaction-demo/`](../../examples/spring-transaction-demo/)（待创建）
>
> 将演示：声明式事务、传播行为对比、事务失效场景复现、编程式事务、readOnly 优化

## 7. 参考链接 / References

- [Spring Framework Reference — Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- [Spring Boot Reference — Transaction](https://docs.spring.io/spring-boot/reference/data/sql.html#data.sql.jpa-and-spring-data.transactional)
- [Baeldung — Spring Transactional](https://www.baeldung.com/transaction-configuration-with-jpa-and-spring)
- [Baeldung — Transaction Propagation](https://www.baeldung.com/spring-transactional-propagation-isolation)
