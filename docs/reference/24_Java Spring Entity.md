---
title: Java Spring Entity
created: 2026-05-08 21:51:13
category: Java-Spring
tags:
  - Java
  - Spring
  - JPA
  - Entity
  - ORM
---

<!-- markdownlint-disable MD025 -->

# Java Spring Entity

## 为什么要学 Entity

前面学了 JPA 的概念和 Spring Data JPA 的便利性，但还缺一个关键环节：**你的 Java 对象和数据库表之间，到底是怎么对应上的？**

这个"对应关系"的定义，就落在 Entity 身上。Entity 是 JPA 的基石 — 没有 Entity 就没有映射，没有映射就没有 ORM。理解了 Entity 的定义规则和生命周期，你才能在正确的时机做正确的操作，避免出现"我以为保存了，结果没保存"或"我以为更新了，结果插了一条新记录"这类诡异问题。

## 核心概念

### Entity 是什么

Entity（实体）是一个被 `@Entity` 注解标注的普通 Java 类（POJO），它的每一个实例对应数据库表中的一行数据。Entity 的字段映射到表的列，Entity 之间的关系映射到表之间的外键。

类比：Entity 就像一张"翻译对照表"。Java 这边叫 `User` 类，数据库那边叫 `users` 表；Java 这边叫 `createdAt` 字段，数据库那边叫 `created_at` 列。JPA 就是按这份对照表来翻译的。

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### 为什么需要 Entity

在 JDBC 时代，数据从数据库到 Java 对象的映射是手动完成的：每写一个查询，就要写一段 `rs.getString("xxx")` 的代码。当表和字段多了以后，这变成了一件极其重复且容易出错的事。

Entity 解决了这个问题：

1. **映射定义一次，到处使用** — Entity 类定义了完整的字段-列对应关系，所有查询都用这同一套映射
2. **编译期类型安全** — `user.getEmail()` 比 `rs.getString("email")` 安全得多，拼写错误在编译期就能发现
3. **关系直观表达** — `@OneToMany`、`@ManyToOne` 直接在代码中表达了表之间的关系，不需要看 ER 图猜关系

### 没有 Entity 会怎样

如果没有 Entity 这套机制，回到手动映射状态：

- 修改了一个字段名，要找到所有引用 `"old_field_name"` 字符串的地方逐一修改
- 表关系靠看外键和手写 JOIN 来维护，代码中看不到任何关系的信息
- DTO、DAO 的字段和数据库列各自定义，三方容易不一致

有了 Entity 之后：字段-列映射定义一次；修改字段名时 IDE 自动重构所有引用；表关系在代码中一目了然。

## 概念深入解释

### Entity 的基本约束

JPA 规范对 Entity 类有四个硬性要求：

1. **必须用 `@Entity` 标注** — 告诉 JPA 这个类需要被管理
2. **必须有 `@Id` 标注的主键字段** — JPA 通过主键来识别和追踪每个 Entity 实例
3. **必须有无参构造器** — JPA 在从数据库加载数据时需要先通过无参构造器创建实例，再通过反射填充字段（`public` 或 `protected` 都可以）
4. **不能是 final 类** — Hibernate 需要通过 CGLIB 或 ByteBuddy 生成代理来实现懒加载，final 类无法被代理

### Entity 的生命周期状态

Entity 在 JPA 管理的整个生命周期中会经历四种状态。理解这些状态是正确使用 JPA 的关键：

| 状态 | 含义 | 如何进入 | 要小心什么 |
|------|------|---------|----------|
| Transient（瞬态） | 刚 `new` 出来的对象，JPA 毫不知情 | `new User()` | `persist()` 被调用前，对这个对象的任何修改都不会反映到数据库 |
| Managed（托管） | JPA 正在追踪这个对象，字段变化会自动同步 | `persist()`、`find()`、`merge()` 返回的对象 | 在事务内修改 Managed 对象的字段，事务提交时会自动生成 UPDATE 语句。这就是"脏检查"机制 |
| Detached（游离） | 曾经被托管，但当前与 JPA 脱钩 | 事务提交后、`detach()` 后 | 修改 Detached 对象不会影响数据库，除非主动 `merge()` 回来 |
| Removed（移除） | 标记为待删除 | `remove()` | 事务提交时生成 DELETE 语句 |

这些状态的变化可以归纳为：

```
Transient  --persist()-->  Managed  --remove()-->  Removed
                            ^ |
                            | | 事务结束 (commit/rollback)
                            | v
                          Detached  --merge()-->  Managed
```

### 主键生成策略

`@GeneratedValue` 的 `strategy` 决定了主键如何生成。选错策略可能导致性能问题或数据冲突：

| 策略 | 原理 | 适用场景 | 注意事项 |
|------|------|---------|---------|
| `IDENTITY` | 依赖数据库自增列（MySQL AUTO_INCREMENT） | 简单应用 | 每次 INSERT 后需要立即查询拿到 ID，Hibernate 无法做 JDBC 批量插入优化 |
| `SEQUENCE` | 使用数据库序列（PostgreSQL、Oracle） | 生产环境推荐 | 支持批量插入，性能优于 IDENTITY。Hibernate 可以预分配一批 ID |
| `TABLE` | 模拟序列，用一张表存当前 ID 值 | 数据库不支持序列时 | 性能最差，所有插入竞争同一行，不推荐 |
| `AUTO` | JPA 自动选择 | 快速原型 | 默认行为，生产环境建议显式指定 |
| `UUID` | Java 生成 UUID 字符串 | 分布式系统 | 主键是字符串，索引性能不如数字；但适合多节点不冲突的 ID 生成 |

对于大多数 Spring Boot + MySQL 项目，`IDENTITY` 够用。对于 PostgreSQL 或需要批量插入的场景，`SEQUENCE` 更好。分布式场景考虑 `UUID` 或雪花算法。

### 字段映射细节

`@Column` 注解可以配置列的详细属性：

| 属性 | 说明 | 示例 |
|------|------|------|
| `name` | 指定列名（默认是字段名转下划线） | `@Column(name = "created_at")` |
| `nullable` | 是否允许 NULL（默认 true） | `@Column(nullable = false)` |
| `unique` | 是否唯一约束 | `@Column(unique = true)` |
| `length` | 字符串列长度（默认 255） | `@Column(length = 500)` |
| `precision` / `scale` | 数值精度和小数位 | `@Column(precision = 10, scale = 2)` |

几个常用的特殊注解：

- `@Enumerated(EnumType.STRING)` — 枚举类型存字符串而非序数（始终用 STRING，不要用 ORDINAL）
- `@Lob` — 标注大对象字段（对应 BLOB / CLOB）
- `@Transient` — 该字段不映射到数据库（用于计算属性或临时数据）
- `@CreatedDate` / `@LastModifiedDate` — 配合 `@EntityListeners(AuditingEntityListener.class)` 自动填充时间戳

### 实体关系映射

这是 Entity 定义中最重要也最容易出错的部分。三种基本关系：

```java
// 多对一: 多个 Order 属于同一个 User
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;

// 一对多: 一个 User 有多个 Order
@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
private List<Order> orders = new ArrayList<>();

// 多对多: 一个 Student 选多门 Course, 一门 Course 有多个 Student
@ManyToMany
@JoinTable(
    name = "student_course",
    joinColumns = @JoinColumn(name = "student_id"),
    inverseJoinColumns = @JoinColumn(name = "course_id")
)
private Set<Course> courses = new HashSet<>();
```

**关键规则：**

- `@ManyToOne` 和 `@OneToOne` 默认 `FetchType.EAGER`（立即加载），**生产环境必须改为 `FetchType.LAZY`**
- `@OneToMany` 和 `@ManyToMany` 默认 `FetchType.LAZY`（懒加载）
- 一对多双向关系中，`mappedBy` 要写在"多"的一方持有的引用上。谁没有外键（没有 `@JoinColumn`），谁就写 `mappedBy`

## 核心要点

1. **Entity 是 ORM 的最小映射单元：** 一个 `@Entity` 类 = 一张表，一个实例 = 一行数据。定义 Entity 就是在定义"翻译对照表"。
2. **四个生命周期状态决定行为：** Transient 对象不会自动保存；Managed 对象的字段变更会自动同步；Detached 对象需要 merge 才能恢复同步。
3. **`FetchType.LAZY` 是你的好朋友：** 不要用默认的 EAGER 加载关联关系，除非你确定每次都需要关联数据。懒加载可以避免加载出不必要的数据。
4. **枚举用 STRING，不要用 ORDINAL：** `@Enumerated(EnumType.STRING)` 把枚举值存为字符串。如果存 ORDINAL（序号），后续在枚举中间插入新值会导致所有数据错乱。
5. **双向关系注意 `mappedBy`：** 双向关系中，`mappedBy` 要写在外键不在自己这边的关联字段上。写反了会导致 JPA 生成多余的中间表。

## 常见误区

- **修改了一个 Detached 对象，以为会自动更新数据库。** 事务提交后 Entity 变成 Detached 状态，此时修改它的字段不会触发任何 SQL。要么在当前事务内修改，要么用 `merge()` 把 Detached 对象重新变为 Managed 后再修改。这是最常见的"为什么没有 UPDATE"问题的原因。
- **`@OneToMany` 的集合用 `null` 而不是空集合初始化。** 最佳实践是在声明时初始化为 `new ArrayList<>()`。如果集合是 `null`，添加第一个元素时会报 NPE；此外 JPA 在某些场景下会用代理替换你的集合，如果原来是 `null` 可能导致代理创建失败。
- **同时使用了 `@OneToMany` 和 `@ManyToOne` 建立双向关系，但没设置 `mappedBy`。** 这会导致 JPA 认为这是两个独立的单向关系，可能生成一张多余的中间表。`mappedBy` 告诉 JPA "这个关系的外键由对方维护，我只是反向引用"。
- **`equals()` 和 `hashCode()` 基于数据库生成的主键。** 新创建的 Entity（Transient 状态）还没有主键，此时调用 `equals()` 可能因为两个对象都是 `null` 主键而判定为"相等"。正确的做法是使用业务键（如 `email` 等唯一字段）或基于类型和 ID 的组合逻辑。
- **在 `@PostLoad` 回调中做耗时操作。** `@PostLoad` 在每次从数据库加载 Entity 后触发，如果在这里做复杂计算或远程调用，会导致性能瓶颈。这个回调应该只做简单的字段初始化和状态校验。

## 与其他概念的关联

- **前置：** [Java Spring ORM 与 JPA](./22_Java%20Spring%20ORM%20与%20JPA.md) -- Entity 是 JPA 的核心概念，需要先理解 JPA 的定位
- **前置：** [Java 注解机制](./02_Java%20注解机制.md) -- `@Entity`、`@Column` 等注解的工作机制依赖注解基础
- **并行：** [Java Spring Data JPA](./23_Java%20Spring%20Data%20JPA.md) -- Spring Data JPA 的操作对象就是 Entity
- **并行：** [Java Spring Repository](./25_Java%20Spring%20Repository.md) -- Repository 接口对 Entity 进行增删改查
- **后续：** [Java Spring 事务管理](./26_Java%20Spring%20事务管理.md) -- Entity 的生命周期状态变更发生在事务上下文中
- **后续：** [Java Spring 响应与 DTO](./18_Java%20Spring%20响应与%20DTO.md) -- Entity 和 DTO 的分层隔离
