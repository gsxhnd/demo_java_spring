# MongoDB — 文档型数据库集成 / MongoDB Integration

> Spring Boot + MongoDB：文档存储、灵活 Schema、聚合管道

## 1. 概述 / Overview

MongoDB 是最流行的文档型 NoSQL 数据库，以 BSON（Binary JSON）格式存储数据。Spring Boot 通过 Spring Data MongoDB 集成。

### 适用场景

| 场景 | 说明 |
|---|---|
| 灵活 Schema | 字段结构频繁变化（用户画像、商品属性） |
| 内容管理 (CMS) | 文章、评论等半结构化数据 |
| 日志 / 事件存储 | 高写入吞吐量 |
| 嵌套文档 | 一对多关系嵌入存储，减少 JOIN |
| 地理空间查询 | 内置 GeoJSON 支持 |

### MongoDB vs MySQL 概念对比

| MySQL | MongoDB |
|---|---|
| Database | Database |
| Table | Collection |
| Row | Document |
| Column | Field |
| PRIMARY KEY | `_id` (ObjectId) |
| JOIN | 嵌套文档 / `$lookup` |

---

## 2. 核心概念 / Core Concepts

### 操作方式

| 方式 | 适用场景 |
|---|---|
| `MongoRepository` | 简单 CRUD，方法名派生查询 |
| `MongoTemplate` | 复杂查询、聚合管道 |
| `@Query` 注解 | 中等复杂度，MongoDB JSON 查询语法 |

### 核心注解速查

| 注解 | 作用 |
|---|---|
| `@Document(collection = "xxx")` | 映射 Collection |
| `@Id` | 主键（映射 `_id`） |
| `@Field("xxx")` | 自定义字段名 |
| `@Indexed` / `@CompoundIndex` | 索引 |
| `@CreatedDate` / `@LastModifiedDate` | 审计字段 |
| `@DBRef` | 文档引用（类似外键，慎用） |

---

## 3. 快速集成 / Quick Start

### Maven 依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

### 配置速查

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://root:mongo123@localhost:27017/demo_db?authSource=admin
```

---

## 4. 进阶要点 / Advanced Topics

- **聚合管道 (Aggregation Pipeline)**：`$match` → `$group` → `$sort` → `$project`，类似 SQL 的 GROUP BY
- **Change Stream**：实时监听数据变更，类似数据库触发器
- **GridFS**：大文件存储（>16MB），Spring Data 提供 `GridFsTemplate`
- **事务支持**：MongoDB 4.0+ 支持多文档事务，需 Replica Set
- **文本索引**：`@TextIndexed` 注解，支持全文搜索
- **TTL 索引**：自动过期删除，适合 Session、验证码等临时数据
- **嵌套 vs 引用**：小数据量嵌套（Embedded），大数据量引用（Reference）

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| `_id` 类型选择 | 默认 ObjectId（String），也可用 Long/UUID |
| 嵌套文档查询 | 用 `.` 路径：`findByProfileCity(String city)` |
| 大文档性能差 | 控制文档大小 <16MB，拆分大数组 |
| 事务不生效 | 需要 Replica Set 模式，单机不支持事务 |
| 时区问题 | MongoDB 存 UTC，应用层转换时区 |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-mongodb-demo/`](../../examples/spring-mongodb-demo/)

包含：Document 实体、MongoRepository、MongoTemplate 复杂查询、聚合管道示例。

启动依赖：
```bash
cd examples/docker-compose && docker compose -f mongodb-compose.yml up -d
```

## 7. 参考链接 / References

- [Spring Data MongoDB 官方文档](https://docs.spring.io/spring-data/mongodb/reference/)
- [MongoDB 官方文档](https://www.mongodb.com/docs/)
