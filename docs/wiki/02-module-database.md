# 数据库集成模块 (database)

> Spring Data 生态：JDBC、JPA、Redis、MongoDB、Elasticsearch、ClickHouse、InfluxDB

## 设计决策

### 为什么需要这个模块

企业级应用的核心是数据存储。Spring 提供了丰富的 Data 模块来简化各种数据库的接入，但每种数据库的最佳实践不同。本模块帮助开发者在实际项目中做出正确的技术选型和集成方案。

### 为什么这么设计

- **选择了**：为每种数据库独立编写文档和示例，按关系型 → 缓存 → 文档 → 搜索 → 分析 → 时序推进
- **而不是**：只做 MySQL + Redis 的"标准"组合
- **原因**：实际项目中数据库选型多样化，开发者需要了解各种数据库在 Spring 中的集成方式

## 关键类型与接口

### 示例项目列表

| 项目 | 数据库 | Spring Data 模块 |
|------|--------|------------------|
| spring-mysql-demo | MySQL 8.0 | Spring Data JPA + MyBatis-Plus |
| spring-redis-demo | Redis 7 | Spring Data Redis |
| spring-mongodb-demo | MongoDB 7 | Spring Data MongoDB |
| spring-es-demo | Elasticsearch 8.15 | Spring Data Elasticsearch |
| spring-clickhouse-demo | ClickHouse | JDBC Template |
| spring-influxdb-demo | InfluxDB 2.7 | InfluxDB Java Client |

## 模块结构

```text
docs/reference/database/
├── README.md           # 数据库篇索引（含环境启动指引）
├── mysql.md            # MySQL + JPA + MyBatis
├── postgresql.md       # PostgreSQL + JSONB + 全文搜索
├── redis.md            # Redis 缓存、分布式锁
├── mongodb.md          # MongoDB 文档存储、聚合管道
├── elasticsearch.md    # ES 全文搜索、聚合分析
├── clickhouse.md       # ClickHouse 列式分析
└── influxdb.md         # InfluxDB 时序数据

examples/
├── spring-mysql-demo/
├── spring-redis-demo/
├── spring-mongodb-demo/
├── spring-es-demo/
├── spring-clickhouse-demo/
└── spring-influxdb-demo/
```

## 与其他模块的关系

### 依赖

- **核心基础模块**：基于 IoC 注入 DataSource / RedisTemplate / MongoTemplate
- **框架核心模块**：结合 Cache 抽象、事务管理

### 被依赖

- **微服务模块**：微服务各自独立的数据存储

## 详细技术参考

以下为各数据库的完整技术参考文档，包含核心概念、配置示例、代码片段、进阶要点和常见问题：

| 数据库 | 参考文档 |
|--------|----------|
| MySQL | [reference/database/mysql.md](../reference/database/mysql.md) |
| PostgreSQL | [reference/database/postgresql.md](../reference/database/postgresql.md) |
| Redis | [reference/database/redis.md](../reference/database/redis.md) |
| MongoDB | [reference/database/mongodb.md](../reference/database/mongodb.md) |
| Elasticsearch | [reference/database/elasticsearch.md](../reference/database/elasticsearch.md) |
| ClickHouse | [reference/database/clickhouse.md](../reference/database/clickhouse.md) |
| InfluxDB | [reference/database/influxdb.md](../reference/database/influxdb.md) |

## 注意事项

- 不同数据库的事务模型差异大（MySQL 支持 ACID，MongoDB 单文档事务，ES 不支持事务），文档需明确指出
- MyBatis vs JPA 是经典的 ORM 选型问题，需要客观对比而非偏袒
- ClickHouse 和 InfluxDB 的 SQL 方言与标准 SQL 差异较大，文档应给出对比示例
- 所有数据库的连接信息与 Docker Compose 配置一致，避免配置不一致导致连接失败
- Redis 示例需区分单机/哨兵/集群模式，但 MVP 阶段先覆盖单机模式
