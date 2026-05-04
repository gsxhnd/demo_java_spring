# 数据库篇 / Database Integration

> Spring Boot 集成主流数据库的完整指南

## 概览 / Overview

Spring 生态提供了丰富的数据库集成方案，从传统关系型数据库到 NoSQL、搜索引擎、时序数据库，几乎覆盖了所有主流数据存储。

### 数据库选型对比 / Database Comparison

| 数据库 | 类型 | 适用场景 | Spring 集成方式 | 数据模型 |
|---|---|---|---|---|
| MySQL | 关系型 RDBMS | 通用业务数据、事务处理 | Spring Data JPA / MyBatis | 表 + 行 |
| PostgreSQL | 关系型 RDBMS | 复杂查询、JSON 混合存储、GIS | Spring Data JPA | 表 + 行 + JSONB |
| Redis | 内存 KV | 缓存、Session、排行榜、分布式锁 | Spring Data Redis | Key-Value |
| MongoDB | 文档型 NoSQL | 灵活 Schema、内容管理、日志 | Spring Data MongoDB | Document (BSON) |
| Elasticsearch | 搜索引擎 | 全文搜索、日志分析、聚合统计 | Spring Data Elasticsearch | Document (JSON) |
| ClickHouse | 列式分析 | OLAP 分析、实时报表、大数据查询 | JDBC / ClickHouse Client | 列式表 |
| InfluxDB | 时序数据库 | 监控指标、IoT 传感器、时间线数据 | InfluxDB Java Client | Measurement + Tag + Field |

### Spring Data 统一抽象 / Spring Data Unified Abstraction

Spring Data 提供了统一的 Repository 编程模型，不同数据库共享相同的接口风格：

```java
// 无论底层是 MySQL、MongoDB 还是 Elasticsearch，接口风格一致
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUsername(String username);
    List<User> findByAgeGreaterThan(int age);
}
```

```
┌──────────────────────────────────────────────────┐
│              Spring Data Commons                  │
│         (Repository, CrudRepository, etc.)        │
├──────────┬──────────┬──────────┬─────────────────┤
│ JPA      │ Redis    │ MongoDB  │ Elasticsearch   │
│ (MySQL/  │ (Lettuce │ (Mongo   │ (RestClient)    │
│  PG)     │  /Jedis) │  Driver) │                 │
├──────────┼──────────┼──────────┼─────────────────┤
│ MySQL    │ Redis    │ MongoDB  │ Elasticsearch   │
│ PostgreSQL│ Server  │ Server   │ Cluster         │
└──────────┴──────────┴──────────┴─────────────────┘
```

### 连接池 / Connection Pool

Spring Boot 默认使用 HikariCP 作为 JDBC 连接池（关系型数据库），其他数据库使用各自的连接管理：

| 数据库 | 连接方式 | 默认客户端 |
|---|---|---|
| MySQL / PostgreSQL | JDBC + HikariCP | HikariCP |
| Redis | 连接池 | Lettuce (默认) / Jedis |
| MongoDB | 连接池 | MongoDB Java Driver |
| Elasticsearch | HTTP Client | Elasticsearch Java Client |
| ClickHouse | JDBC / HTTP | ClickHouse JDBC Driver |
| InfluxDB | HTTP Client | InfluxDB Java Client |

---

## 文档列表 / Documents

1. [MySQL — 关系型数据库 + JPA / MyBatis](mysql.md)
2. [PostgreSQL — 高级关系型数据库](postgresql.md)
3. [Redis — 缓存与 KV 存储](redis.md)
4. [MongoDB — 文档型数据库](mongodb.md)
5. [Elasticsearch — 搜索引擎](elasticsearch.md)
6. [ClickHouse — 列式分析数据库](clickhouse.md)
7. [InfluxDB — 时序数据库](influxdb.md)

## 示例项目 / Example Projects

每个数据库都有独立的示例项目，位于 `examples/` 目录下。所有中间件均提供 Docker Compose 文件，一键启动：

```bash
# 启动单个数据库
cd devops
docker compose -f mysql-compose.yml up -d

# 启动所有数据库
docker compose -f full-stack-compose.yml up -d
```

## 通用依赖说明 / Common Dependencies

所有示例项目基于：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.5</version>
</parent>

<properties>
    <java.version>21</java.version>
</properties>
```
