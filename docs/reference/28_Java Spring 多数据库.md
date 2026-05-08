---
title: Java Spring 多数据库
created: 2026-05-08 21:51:13
category: Java-Spring
tags:
  - Java
  - Spring
  - 多数据源
  - Redis
  - MongoDB
  - MySQL
---

<!-- markdownlint-disable MD025 -->

# Java Spring 多数据库

## 为什么要学多数据库

前面几节一直在用单一的关系型数据库（MySQL/PostgreSQL）讲解 JPA、Entity、Repository、事务管理。但真实项目很少只用一个数据库。

你去看一个典型的中型项目，数据存储方案可能长这样：

- **MySQL** — 用户、订单、商品等核心业务数据
- **Redis** — 会话缓存、热门数据、分布式锁、限流计数器
- **MongoDB** — 日志、用户行为、灵活 Schema 的配置数据
- **Elasticsearch** — 全文搜索、商品检索

每个数据库解决不同的问题。只掌握 JPA + MySQL 是不够的 — 你需要知道如何在一个 Spring Boot 应用中同时接入多种数据库，以及如何管理它们的配置、事务、和生命周期。

这一节不会深入讲每个数据库的内部原理，而是聚焦于 Spring Boot 如何集成它们，以及多数据源共存时的架构考量。

## 核心概念

### 多数据源是什么

多数据源（Multiple DataSources）是指在一个 Spring Boot 应用中同时配置多个独立的数据库连接，每个连接指向不同的数据库实例或不同的数据库类型。每个 DataSource 有自己的连接池、事务管理器、以及对应的持久化框架（JPA/MyBatis/RedisTemplate 等）。

类比：一个应用连接多个数据库，就像你家同时装着电、水、天然气三种管道。每种管道有自己的源头（数据源）、自己的仪表（连接池）、自己的安全阀（事务管理）。它们独立运作，但都由你的房子（应用）统一调度。

```java
// 不同的数据库有不同的接入方式
jdbc:mysql://localhost:3306/main_db       // 关系型数据库
redis://localhost:6379                     // 缓存
mongodb://localhost:27017/logs             // 文档数据库
```

### 为什么需要多数据库

如果用单一数据库"一锅炖"，会遇到这些问题：

- **用 MySQL 做缓存** — 查询成本和连接开销远高于 Redis，而且缓存数据和业务数据耦合在同一个 Schema 中，难以独立扩缩容
- **用 MySQL 做全文搜索** — MySQL 的 `LIKE '%keyword%'` 不走索引，性能极差。Elasticsearch 的倒排索引专门为全文搜索设计
- **用 MySQL 存大量非结构化数据** — 日志、JSON 配置、用户行为数据，Schema 多变、写入量大、不需要事务，用 MongoDB 更合适

每个数据库都有自己的设计哲学和最佳场景：

| 数据库 | 定位 | Spring 集成模块 | 典型场景 |
|--------|------|----------------|---------|
| MySQL / PostgreSQL | 关系型数据库 | Spring Data JPA / JDBC | 核心业务数据 |
| Redis | 内存数据结构存储 | Spring Data Redis | 缓存、会话、分布式锁、排行榜 |
| MongoDB | 文档数据库 | Spring Data MongoDB | 灵活 Schema、日志、内容管理 |
| Elasticsearch | 全文搜索引擎 | Spring Data Elasticsearch | 商品搜索、日志分析 |
| ClickHouse | OLAP 列存数据库 | JDBC | 数据分析、实时报表 |
| InfluxDB | 时序数据库 | InfluxDB Java Client | 监控指标、IoT 传感器数据 |

### 没有多数据库支持会怎样

如果 Spring Boot 不支持多数据源配置，你只能：

- 在一个应用中只连一个数据库 → 缓存、搜索、日志都和主数据库耦合在一起，性能和架构都受限
- 新建一个独立应用专门处理搜索 → 增加部署和维护的复杂度
- 在代码层手动管理多个连接 → 重复造轮子，连接池、事务管理、异常处理全部手写

Spring Boot 的多数据源支持让你在一个应用内优雅地管理多个数据库连接，每种数据库都有对应的 Spring Data 模块提供统一编程体验。

## 概念深入解释

### Spring Data 家族的统一抽象

Spring Data 的设计哲学是"统一接口，不同实现"。无论你用的是 JPA、Redis、MongoDB 还是 Elasticsearch，编程模型是一致的：

```
数据访问层统一抽象
┌────────────────────────────────────────────────────────┐
│                  Spring Data Commons                   │
│        (Repository<T, ID> 接口、分页、审计等)            │
├────────────┬──────────────┬────────────┬───────────────┤
│ Spring     │ Spring       │ Spring     │ Spring        │
│ Data JPA   │ Data Redis   │ Data       │ Data ES       │
│            │              │ MongoDB    │               │
├────────────┼──────────────┼────────────┼───────────────┤
│ Hibernate  │ Jedis/Lettuce│ MongoDB    │ Elasticsearch │
│            │              │ Driver     │ Client        │
└────────────┴──────────────┴────────────┴───────────────┘
```

这意味着：你在 JPA 中学到的 Repository 模式、分页排序、`@Transactional`，在 Redis 和 MongoDB 中依然适用。学习成本因为统一的编程模型而大幅降低。

### 多数据源配置模式

Spring Boot 中配置多个同类型数据源（比如两个 MySQL 数据库），通常有两种模式：

**模式一：配置类手动指定**

```java
@Configuration
public class PrimaryDataSourceConfig {
    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEmf(
            EntityManagerFactoryBuilder builder,
            @Qualifier("primaryDataSource") DataSource dataSource) {
        return builder
            .dataSource(dataSource)
            .packages("com.example.entity.primary")
            .build();
    }
}
```

然后定义第二个数据源的配置类，去掉 `@Primary`，指定不同的 `packages` 扫描路径。两个数据源的 Entity 分别放在不同的包下，互不干扰。

**模式二：动态数据源路由**

通过继承 `AbstractRoutingDataSource`，在运行时根据上下文（如租户 ID、读写标记）动态选择数据源。适合同一类型数据库的读写分离或分库场景。

但对于**不同类型的数据库**（如 MySQL + Redis + MongoDB），配置更简单 — 它们各有各的 Starter 和配置前缀，互不冲突：

```yaml
spring:
  datasource:        # MySQL
    url: jdbc:mysql://localhost:3306/main_db
  data:
    redis:           # Redis
      host: localhost
      port: 6379
    mongodb:         # MongoDB
      uri: mongodb://localhost:27017/logs
```

### Redis 集成要点

Spring Data Redis 的核心组件：

| 组件 | 作用 |
|------|------|
| `RedisTemplate<K, V>` | 通用 Redis 操作模板，支持任意类型 |
| `StringRedisTemplate` | `RedisTemplate<String, String>` 的特化版本 |
| `@RedisHash` | 标注在类上，将对象存为 Redis Hash |
| `CrudRepository` | 对 Redis Hash 操作的 Repository 接口 |

连接方式选择：

- **Jedis** — 老牌 Redis Java 客户端，API 简洁，连接池成熟。适合中小规模
- **Lettuce** — 基于 Netty 的异步客户端，Spring Boot 2.x 后默认使用。性能更好，支持响应式编程

常用操作示例：

```java
// 缓存操作
redisTemplate.opsForValue().set("user:1", user, Duration.ofMinutes(30));
User cached = (User) redisTemplate.opsForValue().get("user:1");

// 过期监听（配合 @RedisHash + TTL）
redisTemplate.expire("key", Duration.ofHours(1));

// 分布式锁（通过 SETNX + TTL 实现）
Boolean locked = redisTemplate.opsForValue()
    .setIfAbsent("lock:order:1", "locked", Duration.ofSeconds(10));
```

### MongoDB 集成要点

MongoDB 是文档数据库，其数据模型和关系型数据库完全不同：

```
关系型          →    MongoDB
Database        →    Database
Table           →    Collection
Row             →    Document (JSON)
Column          →    Field
```

Spring Data MongoDB 提供了与 JPA 类似的 Repository 编程模型：

```java
@Document(collection = "user_logs")
public class UserLog {
    @Id
    private String id;
    private Long userId;
    private String action;
    private Map<String, Object> details;  // 灵活字段
    private Date createdAt;
}

public interface UserLogRepository extends MongoRepository<UserLog, String> {
    List<UserLog> findByUserIdAndAction(Long userId, String action);
    List<UserLog> findByCreatedAtAfter(Date since, Pageable pageable);
}
```

### 多数据库的事务考量

不同数据库有不同的事务能力：

| 数据库 | 事务支持 | 说明 |
|--------|---------|------|
| MySQL/PostgreSQL | 完整 ACID | 标准事务，`@Transactional` 管用 |
| Redis | 有限（MULTI/EXEC） | 不支持回滚，Redis 事务本质是批量执行 |
| MongoDB | 部分（单文档 ACID，4.0+ 多文档） | 副本集配置下支持多文档事务 |
| Elasticsearch | 不支持 | 无事务概念，靠版本控制和乐观锁 |

**关键结论：不要跨数据库类型做事务。** 不要在 `@Transactional` 里同时操作 MySQL 和 Redis，因为 Redis 不会回滚。正确的做法是：先用 MySQL 事务确保核心数据一致性，成功后再异步更新 Redis，失败时用补偿逻辑处理。

## 核心要点

1. **为场景选数据库，不要一库到底：** 缓存用 Redis、全文搜索用 ES、灵活文档用 MongoDB、核心业务用 MySQL。每个数据库有自己擅长的领域。
2. **Spring Data 提供统一感受：** 不论底层是 JPA、Redis 还是 MongoDB，编程模型都是 Repository + Template，学习成本低。
3. **同类型多数据源用 @Primary + @Qualifier：** 两个 MySQL 库 → 两个 DataSource Bean，用包路径区分 Entity 扫描范围。
4. **不同类型数据库配置互不干扰：** MySQL、Redis、MongoDB 各有独立配置前缀，按 Starter 文档配置即可。spring-boot-starter-data-redis 不会和 spring-boot-starter-data-jpa 冲突。
5. **不要跨数据库类型做事务：** Redis 不支持 ACID 回滚。跨数据库操作只能用最终一致性方案（异步补偿、事件驱动），不能用 `@Transactional`。

## 常见误区

- **在 @Transactional 中同时写 MySQL 和 Redis，以为会一起回滚。** Redis 不支持回滚。MySQL 的回滚不会触发 Redis 的数据撤销。如果你的逻辑依赖"MySQL 写入成功 → 同步更新 Redis"，请把 Redis 更新放在事务提交后（通过 `@TransactionalEventListener` 监听事务提交事件），或在 catch 中手动补偿。
- **配置两个 DataSource 后启动报 "required a single bean, but 2 were found"。** 存在多个同类型 Bean 时，Spring 不知道该注入哪个。解决方案：其中一个 DataSource 加 `@Primary` 标记为主数据源，其他用 `@Qualifier` 在注入处指定 Bean 名称。
- **JPA + MongoDB 的 Repository 写法完全一样，可以互换。** `JpaRepository` 和 `MongoRepository` 虽然都是 `Repository` 的子接口，但它们的行为不同。JPA 有关联映射（`@OneToMany`）、脏检查、懒加载；MongoDB 不支持这些。不能把一个 JPA Entity 的 Repository 直接换成 MongoRepository 就期望跑通 — Entity 的注解和语义要跟着改。
- **MongoDB 不需要设计 Schema，想存什么就存什么。** MongoDB 确实不需要预先定义 Schema，但这不意味着不需要设计。生产环境中，MongoDB 的文档结构设计直接影响查询性能（需要建索引）、数据一致性（字段命名规范）、和存储成本。无 Schema 是工具特性，不是设计豁免。
- **Redis 当数据库用，重要数据只存 Redis。** Redis 的数据在内存中，默认持久化策略（RDB 快照 + AOF）可能丢数据。如果 Redis 挂了且没有从持久化文件恢复，数据就丢了。重要数据的"真理源"应该放在 MySQL/PostgreSQL 中，Redis 只做缓存或临时数据，不要当主数据库用。

## 与其他概念的关联

- **前置：** [Java Spring Boot 配置](./14_Java%20Spring%20Boot%20配置.md) -- 多数据源的配置管理
- **前置：** [Java Spring Data JPA](./23_Java%20Spring%20Data%20JPA.md) -- JPA Repository 模式在 Redis/MongoDB 中也有对应实现
- **前置：** [Java Spring 事务管理](./26_Java%20Spring%20事务管理.md) -- 理解事务边界后才能正确处理跨库事务
- **并行：** [Java Spring MyBatis](./27_Java%20Spring%20MyBatis.md) -- 多数据源时，MyBatis 和 JPA 可以各自管理不同的 DataSource
- **后续：** [Java Spring 缓存](./33_Java%20Spring%20缓存.md) -- Spring Cache 抽象 + Redis 实现，将缓存逻辑从数据库操作中解耦
- **后续：** [Java Spring Cloud 分布式事务](../Spring_Cloud/Java Spring Cloud 分布式事务.md) -- 跨数据库、跨服务的事务协调
