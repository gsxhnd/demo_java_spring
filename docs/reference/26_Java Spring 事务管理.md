---
title: Java Spring 事务管理
created: 2026-05-08 21:51:13
category: Java-Spring
tags:
  - Java
  - Spring
  - Transaction
  - JPA
  - 事务
---

<!-- markdownlint-disable MD025 -->

# Java Spring 事务管理

## 为什么要学事务管理

前面几节学完了 Entity 定义、Repository 查询、JPA 的各种操作能力。你可以在 Repository 里写各种查询，在 Service 里调用它们。但有一个关键问题还没解决：

如果用户下单时，扣了库存、创建了订单、但在扣款时失败了，前面的操作怎么办？

没有事务，你已经扣掉的库存就真的扣掉了 — 订单没生成，库存却少了。这就是"部分成功"问题。事务管理的目标就是保证一组操作"要么全成功，要么全回滚"。

Spring 的事务管理是 Java 生态中最简洁的声明式事务实现。一个 `@Transactional` 注解就能搞定所有事务控制。但它"好用"的背后有很多需要理解的东西 — 为什么有时加了注解事务没生效？为什么同一个类里调用不走事务？传播行为到底是什么意思？

## 核心概念

### @Transactional 是什么

`@Transactional` 是 Spring 的声明式事务注解。把它加在方法或类上，Spring 就会在方法执行前开启事务，方法正常结束后提交事务，方法抛出异常时回滚事务。你不需要写任何 `begin`、`commit`、`rollback` 代码。

换一种说法：`@Transactional` 就像给一个操作序列加了"保险"。操作过程中任何一步出了问题，之前的所有操作都会被"撤销"。这和 Git 的原子提交一样 — 要么所有文件都进入版本库，要么一个都不进。

它的实现方式是 AOP：Spring 为被 `@Transactional` 标注的 Bean 生成代理，在代理中织入事务管理的逻辑。

### 为什么需要声明式事务

在没有声明式事务的时候，手动管理事务是这样的：

```java
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    Connection conn = null;
    try {
        conn = dataSource.getConnection();
        conn.setAutoCommit(false);  // 开启事务

        Account from = accountDao.findById(conn, fromId);
        Account to = accountDao.findById(conn, toId);
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        accountDao.update(conn, from);
        accountDao.update(conn, to);

        conn.commit();  // 提交
    } catch (Exception e) {
        if (conn != null) conn.rollback();  // 回滚
        throw e;
    } finally {
        if (conn != null) conn.close();  // 释放连接
    }
}
```

这段代码中，真正有意义的业务逻辑只有中间 4 行，剩下全是事务和资源管理的模板代码。如果一个项目中有 200 个 Service 方法，这段事务管理代码就要重复 200 次。

声明式事务让这变成：

```java
@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    Account from = accountRepository.findById(fromId).orElseThrow();
    Account to = accountRepository.findById(toId).orElseThrow();
    from.setBalance(from.getBalance().subtract(amount));
    to.setBalance(to.getBalance().add(amount));
    accountRepository.save(from);
    accountRepository.save(to);
}
```

业务逻辑和事务管理完全分离。

### 没有 @Transactional 会怎样

没有 `@Transactional`（或不用声明式事务），你会面临三个层面的问题：

1. **代码层面** — 每个 Service 方法都要写 try-catch-finally 的事务模板代码，业务逻辑被淹没
2. **可靠性层面** — 手动管理事务容易遗漏异常处理，某些异常路径下事务没回滚，数据出现不一致
3. **可测试性层面** — 事务和业务逻辑耦合，测试时无法独立验证业务规则

有了 `@Transactional`：事务代码集中在框架层，业务代码干净纯粹；不论哪种异常路径，框架都保证一致性；测试时可以独立验证业务逻辑。

## 概念深入解释

### 声明式事务的实现原理

Spring 事务的核心是 AOP 代理 + PlatformTransactionManager：

```
调用者
  └── 代理对象 (Spring CGLIB/JDK Proxy)
        ├── TransactionInterceptor 拦截
        │     ├── 开启事务 (PlatformTransactionManager.getTransaction())
        │     ├── 执行原始方法
        │     ├── 成功 → 提交 (commit)
        │     └── 异常 → 回滚 (rollback)
        └── 原始 Bean 的方法
```

这就是为什么在同一个类中，一个非 `@Transactional` 方法调用本类的 `@Transactional` 方法时事务不生效 — 调用走的是 `this` 引用，绕过了 AOP 代理。

### 事务传播行为

当一个 `@Transactional` 方法调用另一个 `@Transactional` 方法时，第二个方法怎么处理事务？这是"传播行为"要回答的问题。

Spring 定义了 7 种传播行为，实际开发中最常用的是 3 种：

| 传播行为 | 含义 | 典型场景 |
|---------|------|---------|
| `REQUIRED`（默认） | 如果当前有事务就加入，没有就新建 | 大多数业务方法 |
| `REQUIRES_NEW` | 无论当前有没有事务，都新建一个独立事务 | 记录审计日志（不回滚）、发送消息（不回滚） |
| `NESTED` | 在当前事务中创建嵌套子事务（保存点） | 批量处理中某条失败不影响其他 |

**REQUIRED vs REQUIRES_NEW 的对比：**

```
REQUIRED:
ServiceA.method()        [事务 T1 开启]
  └── ServiceB.method()  [加入事务 T1]
        └── 抛出异常       [T1 标记为回滚]
  └── ServiceA 的后续操作  [T1 已标记回滚, 不会执行]

REQUIRES_NEW:
ServiceA.method()        [事务 T1 开启]
  └── ServiceB.method()  [事务 T2 开启, T1 挂起]
        └── 抛出异常       [T2 回滚]
  └── ServiceA 的后续操作  [T1 继续, 未受影响]
```

最常犯的错误：以为子方法抛出异常不会影响父方法（默认传播行为下，同一事务中任何一处异常都会导致整体回滚）。

### 事务隔离级别

隔离级别决定了多个并发事务之间如何"看到"彼此的数据。级别越高，数据一致性越好，但并发性能越差。

| 隔离级别 | 脏读 | 不可重复读 | 幻读 | 说明 |
|---------|------|----------|------|------|
| READ_UNCOMMITTED | 会 | 会 | 会 | 基本不用 |
| READ_COMMITTED | 不会 | 会 | 会 | PostgreSQL 默认 |
| REPEATABLE_READ | 不会 | 不会 | 会 | MySQL InnoDB 默认 |
| SERIALIZABLE | 不会 | 不会 | 不会 | 串行执行，性能最差 |

Spring 默认使用数据库的默认隔离级别。大多数场景不需要手动修改。如果涉及"读到数据后基于它做判断再写回"（如库存扣减），可能需要 `REPEATABLE_READ` 或用乐观锁来避免并发问题。

### 回滚规则

`@Transactional` 默认只在遇到 **RuntimeException 和 Error** 时回滚，**checked exception 不回滚**。这是 Java 事务管理中最容易被忽视的规则。

```java
@Transactional
public void process() throws Exception {
    // 这个 IOException 不会触发回滚！
    throw new IOException("file not found");
}
```

如果需要在 checked exception 时也回滚，显式声明：

```java
@Transactional(rollbackFor = Exception.class)
```

生产环境建议始终使用 `@Transactional(rollbackFor = Exception.class)`，避免出现"抛了异常但数据没回滚"的诡异情况。

### 只读事务

```java
@Transactional(readOnly = true)
public User getUser(Long id) { ... }
```

设置为 `readOnly = true` 后，Hibernate 会跳过脏检查（Dirty Checking），省去不必要的性能开销。对于纯查询方法，这是一个低成本优化。不要滥用 — 如果方法内有写操作，会被 Hibernate 忽略或报错。

### 事务失效的常见场景

`@Transactional` 看似简单，但有不少场景下会"静默"失效：

| 场景 | 原因 | 解决方法 |
|------|------|---------|
| 同类中方法调用 | 绕过了 AOP 代理 | 把被调用方法移到另一个 Bean |
| 方法不是 public | Spring 要求 public | 改为 public 或使用 AspectJ 模式 |
| 异常被 try-catch 吞掉 | 异常没抛出，Spring 感知不到 | 不要 catch 掉要触发回滚的异常 |
| 数据库引擎不支持事务 | MyISAM 不支持 | 使用 InnoDB |
| 方法抛出 checked exception | 默认不回滚 | 加上 `rollbackFor = Exception.class` |
| Bean 没有被 Spring 管理 | 不是代理对象 | 确保类被 `@Service` 等注解标记 |

## 核心要点

1. **一个注解解决事务：** `@Transactional` 声明式事务让业务代码和事务管理彻底分离，不需要写任何 begin/commit/rollback 代码。
2. **传播行为默认 REQUIRED：** 被调用方法默认加入当前事务，异常会一起回滚。需要独立事务用 `REQUIRES_NEW`。
3. **默认只回滚 RuntimeException：** checked exception 不回滚。生产环境建议统一使用 `rollbackFor = Exception.class`。
4. **同类调用事务失效：** AOP 代理只拦截外部调用。同类内方法互调不走代理，需要把事务方法抽到独立的 Bean。
5. **读操作用 readOnly：** 纯查询方法设置 `readOnly = true`，跳过 Hibernate 脏检查，是一个简单有效的性能优化。

## 常见误区

- **"我加了 @Transactional，为什么事务没生效？"** 检查清单：1) 是不是同一个类里调用的？2) 方法是不是 public？3) 异常是不是被 try-catch 吃掉了？4) 类是不是被 Spring 管理（有没有 @Service/@Component）？5) 数据库引擎支持事务吗（确认是 InnoDB 不是 MyISAM）？6) 是 checked exception 吗？这六个问题是 90% 以上事务失效的原因。
- **REQUIRED 和 REQUIRES_NEW 搞混。** 子方法用 REQUIRED 意味着和父方法共享一个事务，子方法抛出异常会导致整个事务（包括父方法的所有操作）回滚。很多人以为子方法抛出异常只影响子方法自己的操作 — 这是 REQUIRES_NEW 的行为，不是 REQUIRED。
- **事务越大越好 —— 把整个 Service 方法标上 @Transactional 就行。** 事务范围应该是"需要保证一致性的最小操作单元"。事务太大有两个问题：数据库连接和锁的持有时间变长，影响并发性能；回滚时撤消了不需要回滚的操作。把非关键操作（如发通知、记录日志）放在事务外执行。
- **只读事务就是个提示，没什么用。** `readOnly = true` 不止是提示 — Hibernate 会因此跳过脏检查，对查询性能有明显提升（尤其在返回大量 Entity 时）。但对于写操作，设置 `readOnly = true` 会被 Hibernate 静默忽略，写不进去也不会报错，这是更危险的隐患。
- **@Transactional 放在 Controller 层也可以。** 技术上可以，但不应该。Controller 的职责是处理 HTTP 请求/响应，事务管理是业务逻辑层的关注点。放在 Controller 会导致：1) 事务边界不清晰；2) Controller 中调用的非业务操作（如校验、日志）被包进事务；3) 难以复用和测试。

## 与其他概念的关联

- **前置：** [Java Spring AOP](./08_Java%20Spring%20AOP.md) -- `@Transactional` 的实现基础就是 AOP 代理
- **前置：** [Java Spring Bean](./06_Java%20Spring%20Bean.md) -- 事务代理要求 Bean 由 Spring 管理
- **前置：** [Java Spring ORM 与 JPA](./22_Java%20Spring%20ORM%20与%20JPA.md) -- JPA 操作需要事务上下文
- **并行：** [Java Spring Repository](./25_Java%20Spring%20Repository.md) -- `@Modifying` 查询需要事务，批量操作需要控制事务边界
- **后续：** [Java Spring Service 层](./30_Java%20Spring%20Service%20层.md) -- Service 层是事务边界的最佳位置
- **后续：** [Java Spring Cloud 分布式事务](../Spring_Cloud/Java Spring Cloud 分布式事务.md) -- 单体事务到分布式事务的演进
