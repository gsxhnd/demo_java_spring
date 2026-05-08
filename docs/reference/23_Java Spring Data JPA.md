---
title: Java Spring Data JPA
created: 2026-05-08 21:51:13
category: Java-Spring
tags:
  - Java
  - Spring
  - Spring-Data
  - JPA
  - Repository
---

<!-- markdownlint-disable MD025 -->

# Java Spring Data JPA

## 为什么要学 Spring Data JPA

上一节讲了 JPA 和 Hibernate，我们知道了如何通过 `EntityManager` 来操作数据库：

```java
User user = entityManager.find(User.class, userId);
entityManager.persist(newUser);
```

这比原始 JDBC 好了很多，但仍然有改进空间。每次查询都要写 `EntityManager` 的方法调用，分页、排序、条件查询都需要额外处理。更重要的是，`EntityManager` 的方法签名是通用的（`find(Class, Object)`），你无法一眼看出它能做哪些查询 — 代码即文档的效果很差。

Spring Data JPA 就是来解决这个问题的。它提出了一个非常简洁的思路：**你不用写实现，只需要定义接口。**

## 核心概念

### Spring Data JPA 是什么

Spring Data JPA 是 Spring Data 家族中专为 JPA 提供支持的模块。它在 JPA 之上再封装了一层，核心理念是：你定义一个接口继承 `JpaRepository`，Spring 在运行时自动为你生成实现类。

类比：就像你点外卖时的"再来一单"按钮。你不会再重新选菜、加购物车、填地址 — 点一下按钮，上次的完整流程就被复制执行了。Spring Data JPA 就是那个按钮：你定义接口方法签名，框架自动帮你生成对应的 SQL 和 JDBC 调用。

```java
// 你只需要定义接口
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByEmail(String email);
}

// Spring 自动帮你生成实现，你可以直接注入使用
@Autowired
private UserRepository userRepository;
```

### 为什么需要 Spring Data JPA

直接用 JPA 的 `EntityManager` 写数据访问代码，有三个明显的痛点：

1. **样板代码多** — 每个 Entity 都要写类似的 CRUD 代码，`find`、`persist`、`merge`、`remove` 重复出现
2. **查询方法不直观** — 你需要读方法体的 JPQL 才知道这个方法查的是什么
3. **分页和排序麻烦** — 需要手动构建 Query、设置 firstResult 和 maxResults

Spring Data JPA 的解决方案：

- **CRUD 方法自动提供** — 继承 `JpaRepository` 就自动拥有 `save()`、`findById()`、`findAll()`、`deleteById()` 等方法
- **方法命名约定** — `findByEmail(String email)` 框架自动解析为 `WHERE email = ?`
- **分页排序内置** — `Pageable` 和 `Sort` 参数直接支持

### 没有 Spring Data JPA 会怎样

对比两种写同样功能的方式：

```java
// 纯 JPA 方式
@Repository
public class UserDao {
    @PersistenceContext
    private EntityManager em;

    public User findById(Long id) {
        return em.find(User.class, id);
    }

    public List<User> findByEmail(String email) {
        return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                 .setParameter("email", email)
                 .getResultList();
    }

    public Page<User> findAllPaged(int page, int size) {
        // 需要手动写 count 查询和分页查询...
    }
}
```

```java
// Spring Data JPA 方式
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByEmail(String email);
}
```

一个需要手动写每个查询方法的实现，一个只需要声明方法签名。当 Entity 数量增多时，这种差距会成倍放大。

## 概念深入解释

### Spring Data 仓库体系

Spring Data 定义了一套通用的仓库接口层次：

```
Repository (标记接口)
  └── CrudRepository<T, ID>          — 通用 CRUD 操作
        └── PagingAndSortingRepository<T, ID>  — +分页排序
              └── JpaRepository<T, ID>         — + JPA 特有功能（批量操作、flush）
```

你通常直接继承 `JpaRepository`，它包含了所有常用功能。

### 方法命名约定详解

Spring Data JPA 会根据方法名自动生成查询。规则是：解析方法名中的关键词来构造 SQL 的 WHERE 子句。

| 方法名片段 | SQL 等价 | 示例方法 |
|-----------|---------|---------|
| `findByXxx` | `WHERE xxx = ?` | `findByUsername(String)` |
| `findByXxxAndYyy` | `WHERE xxx = ? AND yyy = ?` | `findByEmailAndStatus(String, Status)` |
| `findByXxxOrYyy` | `WHERE xxx = ? OR yyy = ?` | `findByNameOrCode(String, String)` |
| `findByAgeGreaterThan` | `WHERE age > ?` | `findByAgeGreaterThan(Integer)` |
| `findByCreatedAtBetween` | `WHERE created_at BETWEEN ? AND ?` | `findByCreatedAtBetween(LocalDateTime, LocalDateTime)` |
| `findByNameLike` | `WHERE name LIKE ?` | `findByNameLike(String)` |
| `findByOrdersIsNotEmpty` | `WHERE orders IS NOT EMPTY` | `findByOrdersIsNotEmpty()` |
| `findByNameOrderByAgeDesc` | `WHERE name = ? ORDER BY age DESC` | `findByNameOrderByAgeDesc(String)` |

支持的查询关键词包括：`And`、`Or`、`Between`、`LessThan`、`GreaterThan`、`Like`、`In`、`NotIn`、`IsNull`、`IsNotNull`、`True`、`False`、`OrderBy`、`First`、`Top` 等。

**原理：** Spring Data 在启动时会解析你定义的接口方法名，把它转换成 JPA Criteria 查询对象，最终由 Hibernate 执行。整个过程在应用启动时完成，运行时没有额外开销。

### @Query 注解

当方法命名约定不够用时，可以用 `@Query` 直接写 JPQL 或原生 SQL：

```java
// JPQL 查询 — 操作的是对象不是表
@Query("SELECT u FROM User u WHERE u.email LIKE %:keyword%")
List<User> searchByEmailKeyword(@Param("keyword") String keyword);

// 原生 SQL — 操作的是数据库表
@Query(value = "SELECT * FROM users WHERE created_at > ?1", nativeQuery = true)
List<User> findRecentUsers(LocalDateTime since);
```

**什么时候用 JPQL vs 原生 SQL？**
- JPQL — 大多数场景，享受 ORM 的类型安全和数据库无关性
- 原生 SQL — 需要数据库特有函数、复杂查询优化、或调用存储过程时

### 分页与排序

Spring Data 的分页支持非常简洁：

```java
// 分页 — 返回 Page 对象，包含数据 + 总条数 + 总页数
Page<User> findByStatus(Status status, Pageable pageable);
// 调用: userRepository.findByStatus(ACTIVE, PageRequest.of(0, 20));

// 排序 — 返回排序后的列表
List<User> findByStatus(Status status, Sort sort);
// 调用: userRepository.findByStatus(ACTIVE, Sort.by("createdAt").descending());
```

## 核心要点

1. **接口即实现：** 你定义接口继承 `JpaRepository`，框架生成实现。这是 Spring Data 最核心的设计思想。
2. **方法名即查询：** `findByEmail` 就是 `WHERE email = ?`。遵循命名约定即可生成 SQL，不用写一行 JPQL。
3. **分页排序就两参数：** 方法签名加 `Pageable` 或 `Sort` 参数，分页排序自动生效。
4. **特定场景用 @Query：** 命名约定不够时，`@Query` 写 JPQL 或原生 SQL。不要因为框架方便就在所有场景下生办法凑方法名。
5. **Spring Data JPA 不等于 JPA：** 排查问题时仍然需要理解底层的 JPA 和 Hibernate 行为。

## 常见误区

- **方法名越长越好，把所有条件都塞进一个方法名里。** 当查询条件超过 3 个时，方法名会变得难以阅读（如 `findByEmailAndStatusAndCreatedAtBetweenAndTypeOrderByAgeDesc`）。这种情况应该用 `@Query` 或 Specification 动态查询，而不是硬凑方法名。
- **@Query 中的 SQL 语句在 IDEA 中标红报错，以为是错误的。** @Query 中的 JPQL 操作的是 Java 对象而非数据库表，IDEA 需要额外配置 Data Source 或安装 JPA Buddy 插件才能正确校验。SQL 标红不一定是代码写错了。
- **启动时遇到 "No property xxx found for type Yyy" 错误。** 这个错误说明方法名中的属性名和 Entity 中的字段名不匹配。Spring Data 在启动时验证所有方法名 — 拼写错误或字段不存在会在启动阶段暴露，而不是运行时。
- **`save()` 方法会自动更新已有记录。** 实际上 `save()` 对已有的 Entity（persistent）会走 merge，对新 Entity（transient）会走 persist。这个行为依赖 JPA 的 Entity 状态管理，而不是 Spring Data 特有的逻辑。

## 与其他概念的关联

- **前置：** [Java Spring ORM 与 JPA](./22_Java%20Spring%20ORM%20与%20JPA.md) -- 理解 JPA 和 EntityManager 是使用 Spring Data JPA 的前提
- **前置：** [Java Spring Bean](./06_Java%20Spring%20Bean.md) -- Spring Data 自动生成的 Repository 实现本身就是一个 Bean
- **并行：** [Java Spring Entity](./24_Java%20Spring%20Entity.md) -- Repository 操作的对象就是 Entity
- **并行：** [Java Spring Repository](./25_Java%20Spring%20Repository.md) -- Repository 接口和查询方法的深入展开
- **后续：** [Java Spring 事务管理](./26_Java%20Spring%20事务管理.md) -- Repository 方法的调用通常需要在事务上下文中
- **后续：** [Java Spring 集成测试](./36_Java%20Spring%20集成测试.md) -- `@DataJpaTest` 专用于测试 Repository 层
