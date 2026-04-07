# Spring Data JPA 深入 / Spring Data JPA Advanced

> 在 MySQL 文档中已介绍了 JPA 基础。本篇聚焦 JPA 的进阶能力：动态查询、审计、多数据源、数据库迁移，解决真实项目中的复杂数据访问需求。

## 1. 概述 / Overview

Spring Data JPA 在 JPA（Hibernate）之上提供了 Repository 抽象，大幅减少样板代码。但随着业务复杂度增长，简单的方法名查询不够用，需要掌握 Specification 动态查询、审计、投影、多数据源等进阶能力。

## 2. 核心概念 / Core Concepts

### 2.1 查询方式全景

```
简单 ◄──────────────────────────────────────────────► 复杂

方法名派生查询    @Query (JPQL/SQL)    Specification    QueryDSL    原生 SQL
  findByName      自定义查询语句        动态条件组合      类型安全DSL    复杂报表
```

| 方式 | 适用场景 | 类型安全 | 动态条件 |
|------|---------|---------|---------|
| 方法名派生 | 简单固定条件查询 | 编译期检查 | 不支持 |
| `@Query` JPQL | 中等复杂度查询 | 运行时检查 | 不支持 |
| `@Query` Native SQL | 数据库特有语法 | 无 | 不支持 |
| Specification | 动态组合查询条件 | 编译期检查 | 支持 |
| QueryDSL | 复杂动态查询 | 编译期检查 | 支持 |
| `JdbcTemplate` | 极致性能/复杂 SQL | 无 | 手动拼接 |

### 2.2 Specification 动态查询

Specification 基于 JPA Criteria API，通过 Lambda 组合查询条件，适合搜索/筛选场景。

```
Repository extends JpaSpecificationExecutor<T>
    │
    ▼
Specification<T> = (root, query, cb) -> Predicate
    │
    ├── Specification.where(spec1)
    ├──     .and(spec2)
    ├──     .or(spec3)
    └── → 动态组合任意条件
```

### 2.3 审计 / Auditing

| 注解 | 作用 | 类型 |
|------|------|------|
| `@CreatedDate` | 创建时间 | `LocalDateTime` / `Instant` |
| `@LastModifiedDate` | 最后修改时间 | `LocalDateTime` / `Instant` |
| `@CreatedBy` | 创建人 | `String` / `Long` |
| `@LastModifiedBy` | 最后修改人 | `String` / `Long` |
| `@EntityListeners(AuditingEntityListener.class)` | 启用审计监听 | 类注解 |
| `@EnableJpaAuditing` | 全局启用审计 | 配置类注解 |

> `@CreatedBy` / `@LastModifiedBy` 需要实现 `AuditorAware<T>` 接口，从 SecurityContext 或 ThreadLocal 获取当前用户。

### 2.4 投影 / Projection

| 类型 | 说明 | 适用场景 |
|------|------|---------|
| 接口投影（Closed） | 定义接口，只包含需要的 getter | 只查部分字段，减少数据传输 |
| 接口投影（Open） | getter 上用 `@Value` SpEL 表达式 | 需要计算字段 |
| DTO 投影 | 构造函数表达式 `new com.example.UserDTO(...)` | 直接映射到 DTO |
| 动态投影 | 方法参数传入 Class 类型 | 同一查询返回不同投影 |

### 2.5 多数据源架构

```
┌─────────────────────────────────────────────────┐
│                  Application                     │
│                                                  │
│  ┌──────────────┐        ┌──────────────┐       │
│  │ Primary DS    │        │ Secondary DS  │       │
│  │ (MySQL 主库)  │        │ (MySQL 从库)  │       │
│  │               │        │               │       │
│  │ DataSource    │        │ DataSource    │       │
│  │ EntityManager │        │ EntityManager │       │
│  │ TxManager     │        │ TxManager     │       │
│  │ Repository    │        │ Repository    │       │
│  └──────────────┘        └──────────────┘       │
│                                                  │
│  各数据源独立配置：包扫描路径、实体扫描路径、          │
│  事务管理器名称                                    │
└─────────────────────────────────────────────────┘
```

**多数据源配置要点：**

| 配置项 | 说明 |
|--------|------|
| `@Primary` | 标记主数据源 |
| `@ConfigurationProperties("spring.datasource.primary")` | 绑定数据源配置 |
| `@EnableJpaRepositories(basePackages, entityManagerFactoryRef, transactionManagerRef)` | 指定 Repository 使用哪个数据源 |
| `LocalContainerEntityManagerFactoryBean` | 每个数据源独立的 EntityManagerFactory |

### 2.6 数据库迁移 / Database Migration

| 工具 | Flyway | Liquibase |
|------|--------|-----------|
| 迁移文件格式 | SQL / Java | XML / YAML / JSON / SQL |
| 版本命名 | `V1__desc.sql` | changelog 文件 |
| 回滚支持 | 付费版 | 免费支持 |
| Spring Boot 集成 | `spring-boot-starter-flyway` | `spring-boot-starter-liquibase` |
| 适用场景 | SQL 优先，简单直接 | 多数据库兼容，需要回滚 |

**Flyway 命名规范：**

```
V1__create_user_table.sql          ← 版本迁移（只执行一次）
V2__add_email_column.sql
R__refresh_views.sql               ← 可重复迁移（每次变更都执行）
```

## 3. 快速集成 / Quick Start

### 3.1 Maven 依赖

| 依赖 | 说明 |
|------|------|
| `spring-boot-starter-data-jpa` | JPA + Hibernate + HikariCP |
| `spring-boot-starter-flyway` | Flyway 数据库迁移 |
| `spring-boot-starter-liquibase` | Liquibase 数据库迁移（二选一） |
| `com.querydsl:querydsl-jpa` | QueryDSL（可选） |

### 3.2 关键配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.jpa.hibernate.ddl-auto` | `none` (生产) / `create-drop` (嵌入式DB) | DDL 策略 |
| `spring.jpa.show-sql` | `false` | 打印 SQL |
| `spring.jpa.properties.hibernate.format_sql` | `false` | 格式化 SQL |
| `spring.jpa.open-in-view` | `true` | OSIV（建议关闭） |
| `spring.flyway.enabled` | `true` | 启用 Flyway |
| `spring.flyway.locations` | `classpath:db/migration` | 迁移文件路径 |
| `spring.flyway.baseline-on-migrate` | `false` | 已有数据库时设为 true |

## 4. 进阶要点 / Advanced Topics

- **N+1 问题** — `@EntityGraph` 或 `JOIN FETCH` 解决关联查询的 N+1 问题
- **批量操作优化** — `spring.jpa.properties.hibernate.jdbc.batch_size=50` + `@Modifying` 批量更新
- **乐观锁** — `@Version` 字段实现乐观锁，并发更新时抛出 `OptimisticLockException`
- **逻辑删除** — `@SQLDelete` + `@SQLRestriction`（Hibernate 6.3+）或 `@Where`（旧版）
- **二级缓存** — Hibernate 二级缓存 + Ehcache/Caffeine，适合读多写少的数据
- **读写分离** — `AbstractRoutingDataSource` 根据事务只读属性路由到主/从库
- **自定义 Repository 实现** — 接口 + `Impl` 后缀类，混合自定义方法和 Spring Data 方法
- **Spring Data Envers** — 基于 Hibernate Envers 的数据审计/历史版本追踪

## 5. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| LazyInitializationException | Session 已关闭时访问懒加载属性 | 关闭 OSIV 后用 `@EntityGraph` 或 DTO 投影 |
| N+1 查询 | 关联实体逐条加载 | `JOIN FETCH` 或 `@EntityGraph` |
| ddl-auto 生产数据丢失 | 误用 `create` 或 `create-drop` | 生产环境必须用 `none` + Flyway |
| 批量插入慢 | 未开启 JDBC batch | 配置 `hibernate.jdbc.batch_size` |
| 多数据源事务不一致 | 跨数据源无法用本地事务 | 使用 JTA（Atomikos）或最终一致性方案 |

## 6. 示例项目 / Example

> 示例项目位于 [`examples/spring-jpa-advanced-demo/`](../../examples/spring-jpa-advanced-demo/)（待创建）
>
> 将演示：Specification 动态查询、审计、投影、Flyway 迁移、多数据源配置

## 7. 参考链接 / References

- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/)
- [Hibernate ORM User Guide](https://docs.jboss.org/hibernate/orm/6.6/userguide/html_single/Hibernate_User_Guide.html)
- [Flyway Documentation](https://documentation.red-gate.com/fd)
- [Baeldung — Spring Data JPA](https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa)
