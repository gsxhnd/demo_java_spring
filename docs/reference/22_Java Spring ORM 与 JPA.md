---
title: Java Spring ORM 与 JPA
created: 2026-05-08 21:51:13
category: Java-Spring
tags:
  - Java
  - Spring
  - JPA
  - ORM
  - Hibernate
---

<!-- markdownlint-disable MD025 -->

# Java Spring ORM 与 JPA

## 为什么要学 ORM 和 JPA

上一节我们完成了 Web 层的全套能力：Controller 接收请求、DTO 传输数据、参数校验把关、全局异常兜底、OpenAPI 自动生成文档。一个 API 从入口到出口已经走通了。

但到目前为止，所有数据都是"来去无踪"的 — 请求来了处理完就返回，没有留下任何痕迹。一个真实的应用必须把数据存起来：用户注册的信息、订单记录、商品列表……这些都需要持久化到数据库。

你可能会问：直接写 SQL 不行吗？当然可以。Java 从 JDK 1.1 就提供了 JDBC（Java Database Connectivity），你可以用 `Connection`、`Statement`、`ResultSet` 直接操作数据库。问题在于，当表多了、关系复杂了，手写 SQL 的代码量会爆炸，而且 SQL 字符串散落在业务代码各处，维护起来像噩梦。

这就是 ORM 和 JPA 要解决的问题。它们是 Part 5 的第一站，也是从"能写 API"到"能连数据库"的桥梁。

## 核心概念

### ORM 是什么

ORM（Object-Relational Mapping，对象关系映射）是一种编程技术，它在**关系型数据库的表**和**面向对象语言的类**之间建立映射关系。你操作的是 Java 对象，ORM 框架负责把对象操作翻译成 SQL 并执行。

换个说法：ORM 就像翻译官。你对着翻译官说中文（操作 Java 对象），翻译官帮你把话转成对方能懂的语言（SQL），再把对方的回答转回中文（ResultSet 转对象）。

### 为什么需要 ORM

在没有 ORM 的时候，一段"查询用户并打印名字"的代码大概是这样的：

```java
Connection conn = DriverManager.getConnection(url, user, password);
PreparedStatement stmt = conn.prepareStatement("SELECT id, name, email FROM users WHERE id = ?");
stmt.setInt(1, userId);
ResultSet rs = stmt.executeQuery();
if (rs.next()) {
    String name = rs.getString("name");
    String email = rs.getString("email");
    // 手动组装对象...
}
rs.close();
stmt.close();
conn.close();
```

这段代码有几个痛点：

1. **SQL 和 Java 代码混在一起** — 业务逻辑被大量 JDBC 模板代码淹没
2. **字段名是字符串** — `rs.getString("name")` 拼写错误编译期发现不了
3. **手动管理资源** — Connection、Statement、ResultSet 都需要手动关闭
4. **没有类型安全** — 所有字段都是 `getString`/`getInt`，没有编译期检查

ORM 把这些痛点全部解决了：你定义对象，框架生成 SQL，字段有类型检查，连接自动管理。

### 没有 ORM 会怎样

对比两种开发体验：

| 维度 | 纯 JDBC | 有 ORM |
|------|---------|--------|
| 写查询 | 手写 SQL 字符串 | 操作对象，框架生成 SQL |
| 结果映射 | 逐字段 `rs.getString()` | 自动映射到对象 |
| 资源管理 | 手动 try-finally 关闭 | 框架自动管理 |
| 类型安全 | 运行时才发现拼写错误 | 编译期检查 |
| 代码量 | 一个简单查询 15+ 行 | 1-3 行 |

如果没有 ORM，你会在每个数据访问方法里重复写那些 JDBC 模板代码。一个中型项目可能有上百个查询方法，这些重复代码会成为维护的噩梦 — 改一个字段名可能需要改几十处。

### JPA 是什么

JPA（Java Persistence API）是 Java 官方制定的 ORM 规范。它不是具体的实现，而是一套接口和注解标准。

类比：JPA 就像 USB 标准。USB 定义了接口的形状、电压、协议，但具体的 U 盘、键盘、鼠标是由不同厂商生产的。JPA 定义了 `@Entity`、`@Id`、`EntityManager` 这些接口和注解，具体的实现由 Hibernate、EclipseLink 等厂商提供。

JPA 的核心组件：

- **Entity** — 被映射到数据库表的 Java 类
- **EntityManager** — 管理 Entity 生命周期的接口（增删改查）
- **JPQL** — JPA 查询语言，语法类似 SQL 但操作的是对象而非表
- **Persistence Unit** — 数据库连接 + Entity 集合的配置单元

### 为什么需要 JPA

在 JPA 出现之前，Hibernate 已经是 Java 世界最流行的 ORM 框架。但如果你在项目中直接使用 Hibernate 的 API，你的代码就和 Hibernate 绑死了。哪天想换 EclipseLink 或别的 ORM 实现，需要改遍所有数据访问代码。

JPA 的价值在于**标准化**：

```java
// 使用标准 JPA API — 不绑定任何具体实现
EntityManager em = entityManagerFactory.createEntityManager();
User user = em.find(User.class, userId);
```

不管底层是 Hibernate 还是 EclipseLink，这段代码都不需要改。就和你的代码操作的是 `List` 接口，不关心底层是 `ArrayList` 还是 `LinkedList` 一个道理。

### 没有 JPA 会怎样

如果没有 JPA 这个标准，Java 的 ORM 生态会是什么样？

- **框架锁定** — 你选了 Hibernate 就不能轻易换 EclipseLink，因为 API 完全不同
- **学习成本高** — 每个 ORM 框架有自己的 API、自己的配置方式，换框架等于重新学
- **社区分裂** — 教程、工具、最佳实践都针对特定框架，而不是统一标准
- **Spring Data JPA 无从诞生** — Spring Data JPA 构建在 JPA 标准之上，没有 JPA 就没有这套更简洁的封装

有了 JPA 之后：你学的是 JPA 标准，切换到不同实现只需要换依赖和少量配置。Spring Data JPA 也因此可以在 JPA 之上提供更强大的抽象。

## 概念深入解释

### JDBC → ORM → JPA → Spring Data JPA 的演进层次

```
应用代码层
┌──────────────────────────────────────────────────────┐
│                  Spring Data JPA                     │
│  (定义接口，自动实现 — 只需要写方法签名)                │
├──────────────────────────────────────────────────────┤
│                       JPA                            │
│  (标准接口: EntityManager, @Entity, JPQL)             │
├──────────────────────────────────────────────────────┤
│                    Hibernate                         │
│  (JPA 最流行的实现，提供了缓存、懒加载等高级特性)        │
├──────────────────────────────────────────────────────┤
│                      JDBC                            │
│  (Java 最底层的数据库连接 API)                         │
├──────────────────────────────────────────────────────┤
│                    数据库 (MySQL/PostgreSQL)          │
└──────────────────────────────────────────────────────┘
```

每一层都在上一层的基础上提供更高级的抽象。你可以在任何一层编码，但越往上越简洁。

### JPA 核心注解一览

| 注解 | 作用 | 标注位置 |
|------|------|---------|
| `@Entity` | 声明该类是一个 JPA 实体 | 类 |
| `@Table(name="xxx")` | 指定映射到的表名 | 类 |
| `@Id` | 声明主键字段 | 字段 |
| `@GeneratedValue` | 主键生成策略（自增、UUID 等） | 字段 |
| `@Column` | 指定列名、长度、是否可空等 | 字段 |
| `@OneToMany` | 一对多关系 | 字段 |
| `@ManyToOne` | 多对一关系 | 字段 |
| `@ManyToMany` | 多对多关系 | 字段 |
| `@Transient` | 该字段不映射到数据库 | 字段 |

### Hibernate 与 JPA 的关系

Hibernate 比 JPA 早诞生了约 5 年。JPA 1.0（2006 年）的规范很大程度上就是基于 Hibernate 的设计经验制定的。所以 Hibernate 既是 JPA 的灵感来源，也是 JPA 最成熟的实现。

Hibernate 在 JPA 标准之外还提供了很多额外特性：

- **一级缓存（Session 级别）** — 同一 Session 内相同 ID 的查询只打一次 SQL
- **二级缓存** — 跨 Session 的缓存，可对接 Ehcache、Redis 等
- **HQL** — Hibernate 自己的查询语言，功能比 JPQL 更丰富
- **Criteria API** — 类型安全的动态查询构建方式

实际开发中，你写的代码用的是 JPA 标准注解和接口，但底层享受的是 Hibernate 的实现能力。

## 核心要点

1. **ORM 消除阻抗不匹配：** 关系数据库的行和列 vs 面向对象的类与对象 — ORM 在两者之间架桥，让你用面向对象的方式操作数据库。
2. **JPA 是标准，不是实现：** Hibernate 是 JPA 的实现。代码应该依赖 JPA 接口，而不是直接依赖 Hibernate 的具体类。
3. **Entity 是核心：** JPA 的所有操作都围绕 Entity 展开 — 定义 Entity 就是在定义数据模型。
4. **EntityManager 是大管家：** `persist()`、`find()`、`merge()`、`remove()` — 所有数据库操作都通过它。
5. **Spring Data JPA 是 JPA 的再封装：** Spring 在 JPA 之上又包了一层，让你连 EntityManager 都不用直接操作。

## 常见误区

- **JPA 和 Hibernate 是同一个东西。** JPA 是规范，Hibernate 是实现。你在 `pom.xml` 中引入的是 Hibernate（通过 `spring-boot-starter-data-jpa` 传递引入），但代码里用的是 JPA 注解和接口。这种"面向接口编程"让你可以切换实现。
- **ORM 会让性能变差，不如直接写 SQL。** ORM 在简单 CRUD 场景下生成的 SQL 通常是最优的。但当遇到复杂查询（多表联查、聚合、窗口函数）时，ORM 生成的 SQL 可能不够高效。这就是为什么 JPA 也支持原生 SQL 查询 — 不是非此即彼，而是两者都可以用。
- **学了 Spring Data JPA 就不用学 JPA 了。** Spring Data JPA 是 JPA 的上层封装，它简化了最常见的操作，但理解和排查问题时仍然需要 JPA 的知识。比如 N+1 查询问题、懒加载异常、事务传播 — 这些都是 JPA 层面的概念，Spring Data JPA 遮盖不了。
- **Entity 对象可以直接当 DTO 返回给前端。** 这会导致数据库表结构和 API 契约耦合。表结构一改，API 就跟着变。正确的做法是 Entity 只用于数据访问层，通过 DTO 隔离内外。

## 与其他概念的关联

- **前置：** [Java Spring 响应与 DTO](./18_Java%20Spring%20响应与%20DTO.md) -- DTO 概念为后续 Entity vs DTO 的区分打下基础
- **前置：** [Java Spring Boot 配置](./14_Java%20Spring%20Boot%20配置.md) -- 数据库连接等配置知识已在 Spring Boot 配置中学习
- **并行：** [Java Spring Data JPA](./23_Java%20Spring%20Data%20JPA.md) -- JPA 的上层封装，让 CRUD 更简洁
- **并行：** [Java Spring Entity](./24_Java%20Spring%20Entity.md) -- JPA 实体定义的详细展开
- **后续：** [Java Spring 事务管理](./26_Java%20Spring%20事务管理.md) -- ORM 操作通常需要事务保护
- **后续：** [Java Spring MyBatis](./27_Java%20Spring%20MyBatis.md) -- 另一种持久化方式，适合复杂 SQL 场景
