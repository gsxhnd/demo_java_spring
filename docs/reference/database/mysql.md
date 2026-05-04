# MySQL — 关系型数据库集成 / MySQL Integration

> Spring Boot + MySQL：Spring Data JPA 与 MyBatis 双方案

## 1. 概述 / Overview

MySQL 是最流行的开源关系型数据库，Spring Boot 中有两种主流集成方式：

- **Spring Data JPA**：基于 Hibernate ORM，适合标准 CRUD 和 DDD
- **MyBatis / MyBatis-Plus**：SQL 优先，适合复杂查询和精细化 SQL 控制

| 场景 | 推荐方案 |
|---|---|
| 标准 CRUD、快速开发 | Spring Data JPA |
| 复杂多表关联、动态 SQL | MyBatis / MyBatis-Plus |
| DDD 领域建模 | Spring Data JPA |
| 高性能批量操作 | MyBatis + 原生 SQL |

---

## 2. 核心概念 / Core Concepts

### 架构层次

```
Application Code
       │
  ┌────┴─────┐
  ▼          ▼
JPA/Hibernate  MyBatis
  └────┬─────┘
       ▼
  JDBC + HikariCP
       ▼
  MySQL Server
```

### JPA 核心注解速查

| 注解 | 作用 |
|---|---|
| `@Entity` / `@Table` | 实体类 / 指定表名 |
| `@Id` / `@GeneratedValue` | 主键 / 生成策略 |
| `@Column` | 列映射（名称、长度、约束） |
| `@OneToMany` / `@ManyToOne` | 关联关系 |
| `@Transactional` | 事务管理 |
| `@Query` | 自定义 JPQL / 原生 SQL |

### HikariCP 连接池关键参数

| 参数 | 默认值 | 说明 |
|---|---|---|
| `maximum-pool-size` | 10 | 最大连接数 |
| `minimum-idle` | 10 | 最小空闲连接 |
| `connection-timeout` | 30000ms | 获取连接超时 |
| `max-lifetime` | 1800000ms | 连接最大存活时间 |

---

## 3. 快速集成 / Quick Start

### Maven 依赖

```xml
<!-- JPA 方案 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- MyBatis-Plus 方案（可与 JPA 共存） -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.9</version>
</dependency>
```

### 配置速查

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root123
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: update        # 开发用 update，生产用 none
    show-sql: true

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

---

## 4. 进阶要点 / Advanced Topics

- **JPA 审计**：`@CreatedDate` / `@LastModifiedDate` 自动填充时间戳
- **JPA Specification**：动态条件查询，替代复杂 JPQL
- **MyBatis-Plus 代码生成器**：一键生成 Entity / Mapper / Service / Controller
- **多数据源**：`AbstractRoutingDataSource` 实现读写分离
- **分页**：JPA 用 `Pageable`，MyBatis-Plus 用 `Page<T>` + 分页插件
- **乐观锁**：JPA `@Version`，MyBatis-Plus `@Version` + `OptimisticLockerInnerInterceptor`
- **逻辑删除**：MyBatis-Plus `@TableLogic`

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| N+1 查询问题 | JPA: `@EntityGraph` 或 `JOIN FETCH`；MyBatis: 手写关联查询 |
| 时区不一致 | JDBC URL 加 `serverTimezone=Asia/Shanghai` |
| `ddl-auto=update` 生产风险 | 生产环境必须设为 `none`，用 Flyway/Liquibase 管理 DDL |
| HikariCP 连接泄漏 | 设置 `leak-detection-threshold: 60000` |
| MyBatis 驼峰映射不生效 | 确认 `map-underscore-to-camel-case: true` |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-mysql-demo/`](../../examples/spring-mysql-demo/)

包含：JPA Entity + Repository + Service + Controller，MyBatis-Plus Mapper，分页查询，事务示例。

启动依赖：
```bash
cd devops && docker compose -f mysql-compose.yml up -d
```

## 7. 参考链接 / References

- [Spring Data JPA 官方文档](https://docs.spring.io/spring-data/jpa/reference/)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [HikariCP GitHub](https://github.com/brettwooldridge/HikariCP)
