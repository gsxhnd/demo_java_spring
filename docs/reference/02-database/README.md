# 数据库篇

> Spring Boot 集成主流数据库的完整指南：关系型、缓存、文档、搜索、列式分析、时序。

## 为什么需要这篇

企业级应用的核心是数据存储。Spring 提供了丰富的 Data 模块来简化各种数据库的接入，但每种数据库的最佳实践不同。本篇帮助开发者在实际项目中做出正确的技术选型和集成方案。

每种数据库独立编写文档和示例，按 关系型 → 缓存 → 文档 → 搜索 → 分析 → 时序 推进。

## 数据库选型对比

| 数据库 | 类型 | 适用场景 | Spring 集成方式 | 数据模型 |
|---|---|---|---|---|
| MySQL | 关系型 RDBMS | 通用业务数据、事务处理 | Spring Data JPA / MyBatis | 表 + 行 |
| PostgreSQL | 关系型 RDBMS | 复杂查询、JSON 混合存储、GIS | Spring Data JPA | 表 + 行 + JSONB |
| Redis | 内存 KV | 缓存、Session、排行榜、分布式锁 | Spring Data Redis | Key-Value |
| MongoDB | 文档型 NoSQL | 灵活 Schema、内容管理、日志 | Spring Data MongoDB | Document (BSON) |
| Elasticsearch | 搜索引擎 | 全文搜索、日志分析、聚合统计 | Spring Data Elasticsearch | Document (JSON) |
| ClickHouse | 列式分析 | OLAP 分析、实时报表、大数据查询 | JDBC / ClickHouse Client | 列式表 |
| InfluxDB | 时序数据库 | 监控指标、IoT 传感器、时间线数据 | InfluxDB Java Client | Measurement + Tag + Field |

## Spring Data 统一抽象

Spring Data 提供了统一的 Repository 编程模型，不同数据库共享相同的接口风格：

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

## 连接池

| 数据库 | 连接方式 | 默认客户端 |
|---|---|---|
| MySQL / PostgreSQL | JDBC + HikariCP | HikariCP |
| Redis | 连接池 | Lettuce (默认) / Jedis |
| MongoDB | 连接池 | MongoDB Java Driver |
| Elasticsearch | HTTP Client | Elasticsearch Java Client |
| ClickHouse | JDBC / HTTP | ClickHouse JDBC Driver |
| InfluxDB | HTTP Client | InfluxDB Java Client |

## 文档列表

1. [MySQL — 关系型数据库 + JPA / MyBatis](01-mysql.md)
2. [PostgreSQL — 高级关系型数据库](02-postgresql.md)
3. [Redis — 缓存与 KV 存储](03-redis.md)
4. [MongoDB — 文档型数据库](04-mongodb.md)
5. [Elasticsearch — 搜索引擎](05-elasticsearch.md)
6. [ClickHouse — 列式分析数据库](06-clickhouse.md)
7. [InfluxDB — 时序数据库](07-influxdb.md)

## 示例项目

| 示例项目 | 数据库 | Spring Data 模块 |
|---------|--------|------------------|
| `examples/spring-mysql-demo/` | MySQL 8.0 | Spring Data JPA + MyBatis-Plus |
| `examples/spring-redis-demo/` | Redis 7 | Spring Data Redis |
| `examples/spring-mongodb-demo/` | MongoDB 7 | Spring Data MongoDB |
| `examples/spring-es-demo/` | Elasticsearch 8.15 | Spring Data Elasticsearch |
| `examples/spring-clickhouse-demo/` | ClickHouse | JDBC Template |
| `examples/spring-influxdb-demo/` | InfluxDB 2.7 | InfluxDB Java Client |

中间件通过 Docker Compose 启动：

```bash
cd devops
docker compose -f full-stack-compose.yml up -d mysql redis  # 按需启动
```

## 与其他篇章的关系

- **依赖**：[核心基础篇](../01-core/README.md)（IoC 注入 DataSource / RedisTemplate / MongoTemplate）、[框架核心篇](../03-framework/README.md)（Cache 抽象、事务管理）
- **被依赖**：[微服务篇](../04-microservice/README.md)（各微服务独立数据存储）

## 注意事项

- 不同数据库的事务模型差异大（MySQL 支持 ACID，MongoDB 单文档事务，ES 不支持事务），文档中已明确指出
- MyBatis vs JPA 是经典的 ORM 选型问题，文档客观对比而非偏袒
- ClickHouse 和 InfluxDB 的 SQL 方言与标准 SQL 差异较大，文档给出了对比示例
- 所有数据库的连接信息与 Docker Compose 配置一致
- Redis 示例区分单机/哨兵/集群模式，MVP 阶段先覆盖单机模式
