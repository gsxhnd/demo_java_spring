# PostgreSQL — 高级关系型数据库集成 / PostgreSQL Integration

> Spring Boot + PostgreSQL：JSONB、全文搜索、数组类型等高级特性

## 1. 概述 / Overview

PostgreSQL 是功能最强大的开源关系型数据库。相比 MySQL，它提供 JSONB、数组、全文搜索、GIS 等原生高级特性。Spring Boot 中同样通过 Spring Data JPA 集成。

### PostgreSQL vs MySQL

| 特性 | PostgreSQL | MySQL |
|---|---|---|
| JSONB | 原生支持，可索引查询 | JSON 类型，功能较弱 |
| 全文搜索 | 内置 tsvector/tsquery | FULLTEXT 索引，功能有限 |
| 数组类型 | 原生支持 | 不支持 |
| GIS 地理空间 | PostGIS 扩展（业界标准） | 基础空间支持 |
| 物化视图 | 支持 | 不支持 |
| 复杂查询性能 | 更优 | 简单读写更快 |

---

## 2. 核心概念 / Core Concepts

### PostgreSQL 特有数据类型

| 类型 | 说明 | Java 映射 |
|---|---|---|
| `JSONB` | 二进制 JSON，可索引 | `Map<String, Object>` / 自定义类 |
| `UUID` | 通用唯一标识符 | `java.util.UUID` |
| `ARRAY` | 数组类型 | `String[]` / `List<>` |
| `TSVECTOR` | 全文搜索向量 | 原生 SQL |
| `INET` | IP 地址 | `String` |
| `HSTORE` | 键值对 | `Map<String, String>` |

### JSONB 存储模型

```
┌──────────┬──────────┬───────────────────────┐
│ id (PK)  │ name     │ attributes (JSONB)    │
├──────────┼──────────┼───────────────────────┤
│ 1        │ iPhone   │ {"color":"black",     │
│          │          │  "storage":"128GB"}   │
└──────────┴──────────┴───────────────────────┘
  关系型字段              灵活的文档字段
```

---

## 3. 快速集成 / Quick Start

### Maven 依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<!-- JSONB / Array 类型映射 -->
<dependency>
    <groupId>io.hypersistence</groupId>
    <artifactId>hypersistence-utils-hibernate-63</artifactId>
    <version>3.9.0</version>
</dependency>
```

### 配置速查

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/demo_db
    username: postgres
    password: postgres123
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

---

## 4. 进阶要点 / Advanced Topics

- **JSONB 查询操作符**：`->>` 取文本值、`@>` 包含查询、`?` 键存在查询
- **JSONB GIN 索引**：`CREATE INDEX idx ON t USING GIN(attributes)`，加速 JSONB 查询
- **全文搜索**：`tsvector` + `tsquery` + GIN 索引，中文需安装 `zhparser` 扩展
- **数组操作**：`ANY(array)` 查询、`array_agg()` 聚合
- **物化视图**：`CREATE MATERIALIZED VIEW` + `REFRESH MATERIALIZED VIEW`
- **Advisory Lock**：轻量级应用层分布式锁
- **LISTEN/NOTIFY**：数据库级别的发布订阅

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| JSONB 字段映射失败 | 使用 `hypersistence-utils` 的 `@Type(JsonType.class)` |
| 中文全文搜索不分词 | 安装 `zhparser` 或 `pg_jieba` 扩展 |
| Hibernate Dialect 警告 | 显式配置 `PostgreSQLDialect` |
| 数组类型映射 | 使用 `@Type(StringArrayType.class)` + `columnDefinition = "text[]"` |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-mysql-demo/`](../../examples/spring-mysql-demo/)

**包含功能：**
- MySQL 和 PostgreSQL 双 profile 配置（`application.yml` / `application-postgres.yml`）
- JSONB 类型映射（使用 Hypersistence Utils）
- PostgreSQL 数组类型
- 原生 SQL JSONB 查询
- MyBatis-Plus 双数据库支持

**启动依赖：**
```bash
# MySQL 模式（默认）
cd devops && docker compose -f mysql-compose.yml up -d
mvn spring-boot:run

# PostgreSQL 模式
cd devops && docker compose -f postgresql-compose.yml up -d
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

**API 接口：**
- `POST /api/products` - 创建产品（JSONB 属性）
- `GET /api/products` - 获取所有产品
- `GET /api/products/{id}` - 获取单个产品
- `PUT /api/products/{id}/attributes` - 更新 JSONB 属性
- `POST /api/products/{id}/tags` - 添加标签（数组）
- `GET /api/products/category/{category}` - JSONB 分类查询
- `GET /api/products/tag/{tag}` - 标签数组查询

## 7. 参考链接 / References

- [PostgreSQL 官方文档](https://www.postgresql.org/docs/)
- [Hypersistence Utils](https://github.com/vladmihalcea/hypersistence-utils)
- [Spring Data JPA 官方文档](https://docs.spring.io/spring-data/jpa/reference/)
