# MyBatis / MyBatis-Plus vs JPA — ORM 框架选型 / ORM Framework Comparison

> SQL 优先 vs ORM 优先：MyBatis、MyBatis-Plus、Spring Data JPA 深度对比

## 1. 概述 / Overview

Java 生态中两大 ORM 流派：

- **JPA (Hibernate)**：ORM 优先，面向对象操作数据库，自动生成 SQL
- **MyBatis**：SQL 优先，手写 SQL，灵活控制每一条查询

MyBatis-Plus 是 MyBatis 的增强工具，提供通用 CRUD、代码生成器、分页插件等。

---

## 2. 核心概念 / Core Concepts

### 三者对比

| 特性 | Spring Data JPA | MyBatis | MyBatis-Plus |
|---|---|---|---|
| 理念 | ORM 优先，面向对象 | SQL 优先，手写 SQL | SQL 优先 + 通用 CRUD |
| SQL 控制 | 自动生成（可自定义） | 完全手写 | 简单 CRUD 自动，复杂手写 |
| 学习曲线 | 中（需理解 JPA/Hibernate） | 低（会 SQL 就行） | 低 |
| 开发效率 | 高（简单 CRUD） | 中（需写 XML/注解 SQL） | 高（通用 CRUD + 代码生成） |
| 复杂查询 | JPQL / Specification / 原生 SQL | XML Mapper（强项） | Wrapper 条件构造器 + XML |
| 动态 SQL | Specification / Criteria API | XML if/foreach 标签 | LambdaQueryWrapper（类型安全） |
| 多表关联 | @OneToMany 自动关联（N+1 风险） | 手写 JOIN SQL | 手写 JOIN 或 MPJoin 插件 |
| 缓存 | Hibernate 二级缓存 | MyBatis 二级缓存 | 同 MyBatis |
| 代码生成 | 无（Spring Data 不需要） | MyBatis Generator | 内置代码生成器（强大） |
| 分页 | Pageable 参数 | PageHelper 插件 | 内置分页插件 |
| 逻辑删除 | 需手动实现 | 需手动实现 | @TableLogic 一键配置 |
| 乐观锁 | @Version | 需手动实现 | @Version + 插件 |
| 多租户 | 需手动实现 | 需手动实现 | 内置多租户插件 |

### 架构层次对比

```
JPA 方案：                    MyBatis 方案：
Controller                    Controller
  |                             |
  v                             v
Service                       Service
  |                             v
  v                           Mapper (接口)
JpaRepository (接口)            |
  |                             v
  v                           MyBatis (SQL Mapper)
Hibernate (ORM)                 |
  |                             v
  v                           JDBC + HikariCP
JDBC + HikariCP                 |
  |                             v
  v                           Database
Database
```

### 选型建议

| 场景 | 推荐 |
|---|---|
| 快速原型、简单 CRUD | JPA 或 MyBatis-Plus |
| DDD 领域驱动设计 | JPA |
| 复杂报表查询、多表关联 | MyBatis |
| 国内互联网项目（通用） | MyBatis-Plus |
| 需要精确控制每条 SQL | MyBatis |
| 多数据库兼容（MySQL/PG/Oracle） | JPA（Hibernate Dialect 自动适配） |
| 团队 SQL 能力强 | MyBatis |
| 团队偏好面向对象 | JPA |

---

## 3. 快速集成 / Quick Start

### JPA

- 依赖：`spring-boot-starter-data-jpa`
- 核心：`@Entity` + `JpaRepository` 接口

### MyBatis

- 依赖：`mybatis-spring-boot-starter`
- 核心：`@Mapper` 接口 + XML Mapper 文件（或注解 SQL）

### MyBatis-Plus

- 依赖：`mybatis-plus-spring-boot3-starter`
- 核心：`@TableName` 实体 + `BaseMapper<T>` 接口
- 关键配置：

| 配置 | 说明 |
|---|---|
| `mybatis-plus.mapper-locations` | XML Mapper 路径 |
| `mybatis-plus.global-config.db-config.logic-delete-field` | 逻辑删除字段 |
| `mybatis-plus.global-config.db-config.id-type` | 主键策略 |
| `mybatis-plus.configuration.map-underscore-to-camel-case` | 下划线转驼峰 |

### JPA + MyBatis-Plus 共存

两者可以在同一项目中共存：
- JPA 处理简单 CRUD 和领域模型
- MyBatis-Plus 处理复杂查询和报表
- 共享同一个 DataSource 和事务管理器

---

## 4. 进阶要点 / Advanced Topics

### MyBatis-Plus 特色功能

- **LambdaQueryWrapper**：类型安全的条件构造器，避免字段名硬编码
- **代码生成器**：一键生成 Entity / Mapper / Service / Controller
- **分页插件**：`PaginationInnerInterceptor`，自动分页
- **乐观锁插件**：`OptimisticLockerInnerInterceptor`
- **逻辑删除**：`@TableLogic`，DELETE 变 UPDATE
- **多租户插件**：`TenantLineInnerInterceptor`，自动拼接租户条件
- **自动填充**：`MetaObjectHandler` 自动填充 createTime / updateTime
- **枚举处理**：`@EnumValue` 自动映射枚举

### JPA 进阶

- **Specification**：动态条件查询（类似 MyBatis-Plus Wrapper）
- **Projection**：接口投影，只查询需要的字段
- **EntityGraph**：解决 N+1 查询问题
- **Auditing**：`@CreatedDate` / `@LastModifiedDate` / `@CreatedBy`
- **Flyway / Liquibase**：数据库版本管理（替代 ddl-auto）

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| JPA N+1 查询 | `@EntityGraph` 或 `JOIN FETCH` |
| MyBatis 驼峰映射不生效 | 确认 `map-underscore-to-camel-case: true` |
| MyBatis-Plus 和 JPA 冲突 | 实体类同时加 `@Entity` 和 `@TableName`，注意主键策略一致 |
| 分页不生效 | MyBatis-Plus 需注册 `MybatisPlusInterceptor` + `PaginationInnerInterceptor` |
| 逻辑删除后 JPA 查到已删数据 | JPA 不感知 MP 逻辑删除，需加 `@Where` 注解 |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-mysql-demo/`](../../examples/spring-mysql-demo/)（JPA + MyBatis-Plus 共存示例）

## 7. 参考链接 / References

- [Spring Data JPA 官方文档](https://docs.spring.io/spring-data/jpa/reference/)
- [MyBatis 官方文档](https://mybatis.org/mybatis-3/)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [MyBatis-Plus GitHub](https://github.com/baomidou/mybatis-plus)
