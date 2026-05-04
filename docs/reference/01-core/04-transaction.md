# Spring 事务管理 / Transaction Management

> 事务是数据一致性的保障。Spring 的声明式事务极大简化了事务管理，但 `@Transactional` 的"坑"也是面试和生产中的高频问题。

## 1. 概述 / Overview

Spring 通过 `PlatformTransactionManager` 抽象层统一管理事务，支持 JDBC、JPA、MyBatis、MongoDB 等多种数据访问技术。声明式事务（`@Transactional`）基于 AOP 代理实现，是最常用的方式。

## 2. 术语表 / Glossary

> 以下术语是理解 Spring 事务管理的前提。如果不熟悉 AOP、Bean 等基础概念，建议先阅读 [IoC 与依赖注入](01-ioc-di.md) 和 [Spring MVC](02-spring-mvc.md)。

| 术语 | 定义 | 作用 | 为什么存在 |
|------|------|------|-----------|
| **事务 (Transaction)** | 一组数据库操作的集合，要么全部成功提交（commit），要么全部失败回滚（rollback）。遵循 ACID 四大特性。 | 保证数据一致性——转账时"A 扣钱"和"B 加钱"必须同时成功或同时失败，不会出现"钱扣了但对方没收到"。 | 数据库操作天然是分步的，硬件故障、网络中断、业务异常随时可能发生。事务提供了"原子性"这个安全网。 |
| **`@Transactional`（声明式事务）** | 在方法上加注解即可让方法在事务中执行，无需手动开启/提交/回滚事务。Spring 通过 AOP 代理在方法前后自动管理事务。 | 一行注解替代 `try { beginTx; ... commit; } catch { rollback; }` 的样板代码，事务管理代码和业务代码完全分离。 | 事务管理是横切关注点——100 个 Service 方法都需要事务，但不应在每个方法里写重复的事务控制代码。AOP 代理让它"一次配置，全局生效"。 |
| **PlatformTransactionManager** | Spring 的事务管理器抽象接口，为不同数据访问技术提供统一的事务 API。JDBC 用 `DataSourceTransactionManager`，JPA 用 `JpaTransactionManager`。 | 无论底层是 JDBC、JPA 还是 MyBatis，上层代码都用同一套 `@Transactional` 注解。 | 屏蔽底层差异——不关心事务是数据库连接层面的还是 JPA EntityManager 层面的，只关心"有事务"这个抽象。 |
| **传播行为 (Propagation)** | 定义"一个事务方法被另一个事务方法调用时，该怎么做"。`REQUIRED`=加入已有事务，`REQUIRES_NEW`=挂起当前事务并新建独立事务。 | 控制事务的嵌套策略。例如操作日志应该用 `REQUIRES_NEW`——即使业务事务回滚，日志仍然要落库。 | 实际业务中方法会互相调用，简单的一刀切策略不够用。传播行为提供了 7 种策略，覆盖从"必须独立"到"禁止事务"的全场景。 |
| **隔离级别 (Isolation)** | 定义并发事务之间"看到对方数据的程度"。高隔离级别（SERIALIZABLE）数据最一致但性能最差，低隔离级别（READ_UNCOMMITTED）性能最好但可能读到脏数据。 | 在数据一致性和并发性能之间做权衡。 | 数据库是共享资源，多个事务同时读写同一行数据必然产生竞争。隔离级别提供了标准化的取舍方案（解决脏读、不可重复读、幻读）。 |
| **rollbackFor / noRollbackFor** | 指定哪些异常触发回滚、哪些不回滚。默认只对 `RuntimeException` 和 `Error` 回滚，checked exception 不回滚。 | 精确控制回滚策略。例如"余额不足"应回滚，但"邮件通知失败"不应回滚主业务。 | Spring 继承了 EJB 的回滚约定（unchecked exception 回滚），但这个约定不完全符合现代业务需求。显式声明让回滚行为可预测。 |
| **readOnly** | 标记事务为只读，给数据库和 ORM 框架一个优化提示。JPA 会关闭脏检查，某些数据库驱动会路由到只读从库。 | 标记查询类方法，减少不必要的开销。 | 写操作和读操作对数据库的压力完全不同。只读标记让基础设施层可以做针对性的性能优化（如读写分离）。 |
| **AOP 代理（事务代理）** | Spring 为 `@Transactional` Bean 创建代理对象，在方法调用前后插入事务管理逻辑。你调用的"Service"其实是代理，真正的 Service 在代理内部。 | 在不修改业务代码的前提下织入事务管理代码。 | 面向切面编程的核心价值——事务、日志、缓存、安全这些横切逻辑不能靠"复制粘贴到每个方法"来实现。代理对象是透明的中间层。 |

## 3. 核心概念 / Core Concepts

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

## 4. 快速集成 / Quick Start

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

## 5. 设计决策与实现原理 / Design Decisions

> 以下结合 [`examples/spring-transaction-demo/`](../../examples/spring-transaction-demo/) 的实际代码，解释每个设计选择背后的"为什么"。

### 4.1 为什么选择 `JdbcTemplate` 而非 JPA 来做事务 Demo？

```java
@Repository
public class AccountRepository {
    private final JdbcTemplate jdbcTemplate;
    public void updateBalance(String accountNo, BigDecimal newBalance) {
        jdbcTemplate.update("UPDATE account SET balance = ? WHERE account_no = ?", ...);
    }
}
```

- **事务边界更清晰**：JPA 的持久化上下文（Persistence Context）会延迟 SQL 执行，事务边界和实际 SQL 执行时机可能分离，不利于教学观察
- **直接控制 SQL**：`JdbcTemplate` 每条 SQL 立即执行，配合 `TRACE` 日志可直接看到事务的 begin/commit/rollback
- **无 ORM 干扰**：JPA 的脏检查、一级缓存、flush 策略等机制会增加理解事务的复杂度

### 4.2 为什么使用 H2 内存数据库？

- **零环境依赖**：`clone` + `mvn spring-boot:run` 即可运行，无需安装 MySQL/PostgreSQL
- **事务支持完善**：H2 完整支持 ACID 事务（`SET MODE=MySQL` 兼容语法）
- **`DB_CLOSE_DELAY=-1`**：JVM 关闭前保持数据库不销毁，支持 H2 Console 在应用运行期间查看数据
- **`ddl: create-drop`**：每次启动重建表结构，保证 Demo 的可重复性——测试数据状态总是已知的

### 4.3 为什么显式添加 `@EnableTransactionManagement`？

```java
@SpringBootApplication
@EnableTransactionManagement   // Spring Boot 自动配置已包含，此处显式声明以示强调
public class SpringTransactionDemoApplication { ... }
```

Spring Boot 的 `DataSourceTransactionManagerAutoConfiguration` 会在 classpath 有 `DataSource` 时自动启用事务管理。显式添加是为了**教学强调**——让学习者注意到"声明式事务需要显式开启"这个知识点，避免误以为 `@Transactional` 是 Spring 内置的开箱即用能力。

### 4.4 为什么显式设置 `rollbackFor = Exception.class`？

```java
@Transactional(rollbackFor = Exception.class)   // ← 显式指定
public void transfer(String from, String to, BigDecimal amount) { ... }
```

Spring 事务的默认回滚策略：**仅对 `RuntimeException` 和 `Error` 回滚**，对 checked exception（如 `IOException`、`SQLException`）不回滚。这是 Spring 团队的历史设计选择（符合 EJB 传统），但在实际业务中：

- 很多 checked exception 同样意味着数据不一致，应该回滚
- `rollbackFor = Exception.class` 是**业界最佳实践**——让所有异常都触发回滚
- Demo 中 `processTransfer()` 抛出的 `RuntimeException` 虽然默认就会回滚，但显式设置 `rollbackFor` 培养了"必加"的编码习惯

### 4.5 为什么 `logOperation()` 使用 `REQUIRES_NEW`？

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public void logOperation(String operation, String entityType, Long entityId, String details) {
    operationLogRepository.save(logEntry);
}
```

这是事务 Demo 中最关键的演示点：

- **审计日志必须独立于业务事务**：转账失败回滚时，操作日志也应被保留（用于问题追溯）
- **`REQUIRES_NEW` 挂起当前事务**：创建一个全新的独立事务，提交/回滚不受外部事务影响
- **典型场景**：订单取消→主事务回滚库存和订单→但"取消操作"这条日志仍写入数据库

### 4.6 为什么 `OrderService` 中 `logOperation()` 调用被 `try-catch` 包裹？

```java
try {
    accountService.logOperation("CREATE_ORDER", "Order", order.getId(), ...);
} catch (Exception e) {
    log.warn("日志记录失败: {}", e.getMessage());  // 不向上抛
}
```

即使日志写入使用了 `REQUIRES_NEW`（独立事务），如果日志写入本身失败（如数据库连接断开），异常仍会传播到 `OrderService` 并触发主事务回滚。`try-catch` 确保：
- **日志写入失败不影响业务**："日志丢了可以补，订单丢了是事故"
- **防御性编程**：非核心操作不应拖垮核心流程

### 4.7 为什么 `readOnly = true` 只加在查询方法上？

```java
@Transactional(readOnly = true)
public List<Account> findAll() { return accountRepository.findAll(); }
```

- **JPA 场景**：`readOnly = true` 关闭 Hibernate 的脏检查（Dirty Checking），减少内存占用
- **JDBC 场景**：`DataSourceTransactionManager` 调用 `Connection.setReadOnly(true)`，部分数据库驱动会据此优化（如路由到只读从库）
- **语义信号**：明确标注"此方法不修改数据"，代码审查者一目了然
- **注意**：Demo 中 `createAccount()` 曾误标 `readOnly = true`（该处为写入操作），这是一个反面教材——`readOnly` 不能无脑添加

### 4.8 为什么用 `CommandLineRunner` 而非 `@PostConstruct` 初始化数据？

```java
@Bean
public CommandLineRunner initData(AccountService accountService) {
    return args -> accountService.initTestData();
}
```

| 对比 | `@PostConstruct` | `CommandLineRunner` |
|------|------------------|---------------------|
| 触发时机 | Bean 初始化完成后（单 Bean 级别） | 应用启动完成后（全局级别） |
| 数据源就绪 | 不确定（DataSource 可能未初始化） | 保证（Spring Boot 自动配置已完成） |
| 事务支持 | 不支持（不在 Spring 代理内） | 支持（`initTestData()` 上的 `@Transactional` 生效） |

Demo 需要事务性地插入初始化数据，因此必须用 `CommandLineRunner`。

### 4.9 为什么事务日志设置为 `TRACE` 级别？

```yaml
logging:
  level:
    org.springframework.transaction: TRACE
    org.springframework.jdbc.datasource.DataSourceTransactionManager: TRACE
```

`TRACE` 级别会打印每次事务的完整生命周期：
```
TRACE o.s.t.i.TransactionInterceptor - Getting transaction for [...]
TRACE o.s.j.d.DataSourceTransactionManager - Acquired Connection [...] for JDBC transaction
TRACE o.s.j.d.DataSourceTransactionManager - Switching JDBC Connection [...] to manual commit
TRACE o.s.t.i.TransactionInterceptor - Completing transaction for [...] after [...]
TRACE o.s.j.d.DataSourceTransactionManager - Initiating transaction commit
```

这些日志是学习事务原理最直观的教学材料——从控制台就能看到事务的获取、提交、回滚全过程。

### 4.10 为什么 `open-in-view: false` 即使项目用 `JdbcTemplate` 也设置？

```yaml
spring:
  jpa:
    open-in-view: false
```

虽然 `spring-transaction-demo` 使用 `JdbcTemplate` 而非 JPA（`open-in-view` 对 JDBC 无影响），但这是项目约定的编码规范（见 [AGENTS.md](AGENTS.md)）。即使当前不使用 JPA，也预设了"如果将来引入 JPA，该配置已正确"。

### 4.11 为什么跨服务调用 `@Transactional` 能生效（`OrderService` 调 `AccountService`）？

```java
// OrderService (Bean A)
accountService.deposit(fromAccountNo, amount);  // ✅ 事务生效

// 如果改成自调用：
this.internalMethod();  // ❌ 事务不生效（未经过代理）
```

关键区别在于对象引用：
- `accountService` 是 Spring 注入的**代理对象**（CGLIB 代理），调用经过代理 → 事务拦截器生效
- `this.internalMethod()` 是**原始对象的直接引用**，不经过代理 → 事务拦截器不生效

这是 Demo 中 `OrderService` 和 `AccountService` 分属两个类的设计原因——展示正确的跨 Bean 事务调用模式。

## 6. 进阶要点 / Advanced Topics

- **`readOnly = true` 优化** — JPA 关闭脏检查（Dirty Checking），JDBC 可能走从库，MySQL 优化查询计划
- **大事务拆分** — 避免在 `@Transactional` 方法中做 RPC、文件 IO 等耗时操作，缩短事务持有连接的时间
- **编程式事务** — `TransactionTemplate` 或 `TransactionOperator`（响应式），适合需要代码块级控制的场景
- **多数据源事务** — 每个数据源配独立的 `TransactionManager`，`@Transactional("orderTxManager")` 指定
- **分布式事务** — 跨服务/跨数据源场景，参考 [分布式事务文档](../04-microservice/06-distributed-transaction.md)
- **OSIV（Open Session In View）** — Spring Boot 默认开启，会延长数据库连接持有时间，建议生产关闭 `spring.jpa.open-in-view=false`
- **事务事件** — `@TransactionalEventListener` 在事务提交后触发事件，适合发送通知、更新缓存等

## 7. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 事务不回滚 | 抛出 checked 异常 | 加 `rollbackFor = Exception.class` |
| 自调用事务不生效 | 未走代理 | 拆分到不同 Service 类 |
| `@Transactional` 在 private 方法上 | 代理无法拦截 | 改为 public |
| 长事务导致连接池耗尽 | 事务方法中有慢操作 | 拆分事务，缩小事务范围 |
| 并发更新丢失 | 缺少乐观锁/悲观锁 | JPA `@Version` 乐观锁或 `SELECT FOR UPDATE` |
| 多数据源事务混乱 | 使用了错误的事务管理器 | 显式指定 `transactionManager` |

## 8. 示例项目 / Example

> 示例项目位于 [`examples/spring-transaction-demo/`](../../examples/spring-transaction-demo/)
>
> 已演示：声明式事务（`@Transactional`）、传播行为（`REQUIRED` / `REQUIRES_NEW`）、`rollbackFor` 显式设置、`readOnly` 查询优化、多表事务原子性（订单+账户扣款）、跨 Service 事务组合、事务失效场景（余额不足、业务规则拦截）、H2 内存数据库 + `JdbcTemplate`、`CommandLineRunner` 数据初始化

## 9. 参考链接 / References

- [Spring Framework Reference — Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- [Spring Boot Reference — Transaction](https://docs.spring.io/spring-boot/reference/data/sql.html#data.sql.jpa-and-spring-data.transactional)
- [Baeldung — Spring Transactional](https://www.baeldung.com/transaction-configuration-with-jpa-and-spring)
- [Baeldung — Transaction Propagation](https://www.baeldung.com/spring-transactional-propagation-isolation)

## 10. 下一步

核心基础篇到此完成。接下来可以根据兴趣选择方向：

- **数据库篇** — 学习 MySQL、Redis、MongoDB 等数据库在 Spring 中的集成 → [数据库篇](../02-database/README.md)
- **框架核心篇** — 深入 Security、AOP、Actuator 等企业级能力 → [框架核心篇](../03-framework/README.md)

建议先学数据库篇（尤其是 MySQL + JPA），再学框架核心篇，因为很多框架能力（如缓存、JPA 深入）依赖数据库基础。
