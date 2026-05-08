---
title: Java Spring Repository
created: 2026-05-08 21:51:13
category: Java-Spring
tags:
  - Java
  - Spring
  - Spring-Data
  - Repository
  - JPA
---

<!-- markdownlint-disable MD025 -->

# Java Spring Repository

## 为什么要学 Repository

前面学完了 Entity 的定义和 Spring Data JPA 的入门使用，你已经知道了怎么定义 Entity、怎么继承 `JpaRepository`、怎么用命名约定生成查询。

但 Spring Data JPA 的查询能力远不止 `findByXxx`。实际开发中，你必然会遇到这些需求：多个动态条件的组合查询、只查某几个字段而不是整个 Entity、批量操作、审计字段自动填充。这些才是日常开发中最常见的场景。

如果只学会了命名约定，遇到这些需求时你会手足无措 — 不知道该用 `@Query` 还是 Specification，不知道投影是什么，不知道审计字段怎么能自动填。这一节就是要帮你把这些"进阶但常用"的能力补上。

## 核心概念

### Repository 是什么

Repository 是 Spring Data 中**数据访问层的核心抽象**。它不是 JPA 的概念，而是 Spring Data 自己提出的一种模式：将数据访问逻辑封装在 Repository 接口中，让 Service 层完全不需要关心底层是 JPA、MongoDB、Redis 还是其他数据源。

类比：Repository 就像餐厅的菜单。你不需要知道厨房（数据库）怎么运作，只需要对着菜单（Repository 接口）点菜（调用方法）。换一个厨师（换数据库实现），菜单不用换，你点的菜也不用换。

Spring Data 的 Repository 体系分为三层：

```
Repository<T, ID>          — 标记接口，没有方法
  └── CrudRepository       — 基础 CRUD
        └── PagingAndSortingRepository  — 带分页排序
              └── JpaRepository         — JPA 专属扩展
```

### 为什么需要 Repository 模式

在没有 Repository 层时，Service 直接调用 DAO（Data Access Object）。这带来两个问题：

1. **Service 和数据库实现耦合** — Service 代码里到处都是 `EntityManager` 或 `JdbcTemplate`，想换成别的持久化方式（如从 MySQL 迁移到 MongoDB），Service 代码要大面积修改
2. **测试困难** — 测试 Service 时必须连真实数据库，或者 mock 掉整个 DAO 层

Repository 模式解决了这两个问题：
- Repository 接口是 Service 层的唯一依赖 — 切换数据源只需更换实现类，Service 代码不变
- 测试 Service 时可以 mock Repository 接口，不需要真实数据库

### 没有 Repository 会怎样

对比两种写法：

无 Repository 层（Service 直接操作 EntityManager）：

```java
@Service
public class UserService {
    @PersistenceContext
    private EntityManager em;

    public User findUser(Long id) {
        return em.find(User.class, id); // 直接依赖 JPA
    }
}
```

有 Repository 层：

```java
@Service
public class UserService {
    private final UserRepository userRepository; // 依赖接口，不依赖实现

    public User findUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}
```

后者在换数据库实现、测试、分层隔离方面都远胜前者。Repository 是你和数据库之间的"缓冲层"。

## 概念深入解释

### Repository 接口的三种扩展方式

当你发现命名约定不够用时，有三种方式扩展 Repository 的能力：

**1. @Query（适用简单自定义查询）**

```java
@Query("SELECT u FROM User u WHERE u.email = :email AND u.status = :status")
List<User> findByEmailAndStatus(@Param("email") String email,
                                 @Param("status") Status status);

@Modifying
@Query("UPDATE User u SET u.status = :status WHERE u.lastLoginAt < :date")
int deactivateInactiveUsers(@Param("status") Status status,
                             @Param("date") LocalDateTime date);
```

注意：`@Modifying` 用于 UPDATE/DELETE 查询，需要配合 `@Transactional` 使用。

**2. Specification（适用动态多条件查询）**

当查询条件由用户在界面上自由组合时（如筛选表单），Specification 是最佳选择：

```java
public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {  // 额外继承这个接口
}

// 使用方式
Specification<User> spec = Specification
    .where(nameContains(keyword))
    .and(statusEquals(Status.ACTIVE))
    .and(createdAfter(startDate));

List<User> users = userRepository.findAll(spec);
```

**3. 自定义实现（适用任何复杂逻辑）**

当以上两种都不够用时，可以在接口中定义带实现的方法：

```java
// 定义自定义接口
public interface CustomUserRepository {
    List<User> findTopActiveUsers(int limit);
}

// 实现它
public class CustomUserRepositoryImpl implements CustomUserRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<User> findTopActiveUsers(int limit) {
        // 写任意复杂的查询逻辑
    }
}

// 让 UserRepository 同时继承 JpaRepository 和自定义接口
public interface UserRepository extends JpaRepository<User, Long>,
        CustomUserRepository { }
```

### 查询结果投影（Projection）

有时候你不需要查整个 Entity，只需要某几个字段。用整个 Entity 太浪费 — 加载了 20 个字段但只用 3 个。

Spring Data 支持三种投影方式：

**接口投影（最常用）：**

```java
// 定义一个接口，只包含需要的字段的 getter
public interface UserSummary {
    String getEmail();
    String getName();
    LocalDateTime getCreatedAt();
}

// Repository 中返回这个接口
List<UserSummary> findByStatus(Status status);
```

Spring 在运行时自动生成代理，只查询 `email`、`name`、`created_at` 三个字段。

**DTO 投影（类投影） — 通过构造器表达式：**

```java
@Query("SELECT new com.example.dto.UserDto(u.id, u.email, u.name) FROM User u")
List<UserDto> findUserDtos();
```

**动态投影 — 同一个查询方法返回不同投影：**

```java
<T> List<T> findByEmail(String email, Class<T> type);

// 调用时指定投影类型
List<User> entities = repo.findByEmail(email, User.class);
List<UserSummary> summaries = repo.findByEmail(email, UserSummary.class);
```

### 审计功能

让 `createdAt` 和 `updatedAt` 这类字段自动填充，而不是每次手动设置：

```java
@Entity
@EntityListeners(AuditingEntityListener.class) // 启用审计监听
public class User {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

// 在 @Configuration 类上添加 @EnableJpaAuditing 启用审计
```

这样每次 `save()` 时，框架自动帮你填创建时间和修改时间。如果再加上 Spring Security，还能用 `@CreatedBy` / `@LastModifiedBy` 自动记录操作人。

### 批量操作

JPA 的 `saveAll()` 默认是逐条 save，大数据量时效率低。几种优化方式：

| 方式 | 说明 | 适用场景 |
|------|------|---------|
| `saveAll(Iterable)` | 批量保存，Hibernate 会尽量合并为批量 INSERT | 几十到几百条 |
| `@Modifying @Query` | 一条 SQL 更新多条记录 | 批量更新状态 |
| `JdbcTemplate.batchUpdate()` | 绕过 JPA，直接用 JDBC 批量操作 | 数千到数万条 |
| `EntityManager.flush()` + `clear()` | 分批 flush 并 clear，避免持久化上下文膨胀 | 手动控制批量导入 |

`saveAll()` 并**不会**自动把多条 INSERT 合并成一条 SQL。它只是在一个事务里循环执行 INSERT。真正的批量 SQL 优化需要配置 `spring.jpa.properties.hibernate.jdbc.batch_size`（如设为 50）并配合 `IDENTITY` 以外的主键生成策略。

## 核心要点

1. **Repository 是数据访问的统一抽象：** Service 只依赖接口，不依赖具体实现。换数据库、换 ORM 时，Service 代码不受影响。
2. **命名约定适合固定条件查询：** 方法名直接表达查询意图，适合 1-3 个固定条件。条件多了用 Specification。
3. **Specification 处理动态多条件：** 当查询条件是用户自由组合时（如后台筛选表单），Specification 是最优雅的方案。
4. **投影避免查多余字段：** 接口投影只查需要的字段，减少数据传输和内存占用。列表查询场景尤其重要。
5. **审计字段用 `@CreatedDate` + `@EnableJpaAuditing`：** 不需要每次手动 `setCreatedAt(LocalDateTime.now())`，框架自动处理。

## 常见误区

- **把 `JpaRepository` 当万能接口，所有查询都用 `findAll()` 然后手动过滤。** 这是把数据库当内存用。数据库的 WHERE、JOIN、聚合查询比 Java 遍历快几个数量级。应该让 Repository 承担查询职责，而不是把所有数据拉到内存里再过滤。
- **`saveAll()` 就是一条批量 SQL。** 默认不是。需要配置 `jdbc.batch_size` 且主键生成策略不是 IDENTITY，才能真正合并为批量 SQL。否则 `saveAll(1000条)` 就是循环执行 1000 次 INSERT。
- **在 `@Query` 中写 `SELECT *`。** JPQL 不支持 `SELECT *`，因为 JPQL 操作的是对象不是表。应该写 `SELECT u`（查询整个对象）或 `SELECT u.name, u.email`（查询特定字段）。
- **Specification 中的条件顺序和 SQL 执行顺序不一致。** Specification 的 `and()` / `or()` 组合体现的是查询条件的逻辑关系，但最终生成的 SQL WHERE 子句顺序由 Hibernate 优化。不要在 Specification 层面依赖条件求值的先后顺序（如短路求值）。
- **忘记加 `@Modifying` 导致 UPDATE/DELETE @Query 报错。** JPA 规范要求修改数据的查询必须用 `@Modifying` 标注，否则会抛出 `TransactionRequiredException`。同时这类方法必须加 `@Transactional`。

## 与其他概念的关联

- **前置：** [Java Spring Data JPA](./23_Java%20Spring%20Data%20JPA.md) -- Repository 模式是 Spring Data JPA 设计的核心
- **前置：** [Java Spring Entity](./24_Java%20Spring%20Entity.md) -- Repository 操作的对象就是 Entity
- **并行：** [Java Spring 响应与 DTO](./18_Java%20Spring%20响应与%20DTO.md) -- 投影（Projection）和 DTO 是同一思想在不同层的体现
- **后续：** [Java Spring 事务管理](./26_Java%20Spring%20事务管理.md) -- `@Modifying` 查询和批量操作都需要事务管理
- **后续：** [Java Spring Service 层](./30_Java%20Spring%20Service%20层.md) -- Service 通过 Repository 访问数据
- **后续：** [Java Spring 测试切片](./37_Java%20Spring%20测试切片.md) -- `@DataJpaTest` 专用于测试 Repository 层
