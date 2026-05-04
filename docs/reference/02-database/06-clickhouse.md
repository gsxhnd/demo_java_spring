# ClickHouse — 列式分析数据库集成 / ClickHouse Integration

> Spring Boot + ClickHouse：OLAP 分析、实时报表、大数据查询

## 1. 概述 / Overview

ClickHouse 是 Yandex 开源的列式存储分析数据库，专为 OLAP（在线分析处理）场景设计。查询速度极快，适合处理数十亿行级别的分析查询。

### 适用场景

| 场景 | 说明 |
|---|---|
| 实时报表 | 用户行为分析、业务指标看板 |
| 日志分析 | 替代 ELK 中的 Elasticsearch 做日志聚合 |
| 时序数据分析 | 监控指标聚合（非实时写入场景） |
| 大数据查询 | 数十亿行级别的 Ad-hoc 查询 |
| A/B 测试分析 | 实验数据统计 |

### ClickHouse vs MySQL

| 特性 | ClickHouse | MySQL |
|---|---|---|
| 存储方式 | 列式存储 | 行式存储 |
| 适用场景 | OLAP 分析 | OLTP 事务 |
| 写入方式 | 批量写入（不支持单行 UPDATE/DELETE） | 单行 CRUD |
| 查询速度 | 聚合查询极快（10-100x） | 点查快，聚合慢 |
| 事务 | 不支持 | 完整 ACID |
| JOIN | 支持但不推荐大表 JOIN | 完整支持 |

### 核心特性

- **列式存储**：只读取查询涉及的列，IO 极少
- **向量化执行**：SIMD 指令加速计算
- **数据压缩**：列式存储天然高压缩比（10:1 常见）
- **MergeTree 引擎**：支持主键排序、分区、TTL

---

## 2. 核心概念 / Core Concepts

### 表引擎 / Table Engine

| 引擎 | 说明 | 适用 |
|---|---|---|
| `MergeTree` | 最常用，支持主键排序、分区 | 通用分析表 |
| `ReplacingMergeTree` | 按主键去重（最终一致） | 需要去重的场景 |
| `SummingMergeTree` | 按主键自动聚合求和 | 预聚合报表 |
| `AggregatingMergeTree` | 自定义聚合函数 | 复杂预聚合 |
| `Distributed` | 分布式表（查询路由） | 集群部署 |

### 数据类型速查

| ClickHouse 类型 | Java 映射 | 说明 |
|---|---|---|
| `UInt32` / `Int64` | `Integer` / `Long` | 整数 |
| `Float64` | `Double` | 浮点数 |
| `String` | `String` | 字符串 |
| `DateTime` | `LocalDateTime` | 日期时间 |
| `Date` | `LocalDate` | 日期 |
| `UUID` | `UUID` | UUID |
| `Array(T)` | `List<T>` | 数组 |
| `Nullable(T)` | 包装类型 | 可空 |

---

## 3. 快速集成 / Quick Start

### 方式一：JDBC 集成（推荐入门）

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>com.clickhouse</groupId>
    <artifactId>clickhouse-jdbc</artifactId>
    <version>0.7.1</version>
</dependency>
<!-- LZ4 压缩支持 -->
<dependency>
    <groupId>org.lz4</groupId>
    <artifactId>lz4-java</artifactId>
    <version>1.8.0</version>
</dependency>
```

```yaml
spring:
  datasource:
    url: jdbc:clickhouse://localhost:8123/demo_db
    username: default
    password: clickhouse123
    driver-class-name: com.clickhouse.jdbc.ClickHouseDriver
    hikari:
      maximum-pool-size: 10
```

### 方式二：ClickHouse Java Client（原生客户端）

```xml
<dependency>
    <groupId>com.clickhouse</groupId>
    <artifactId>client-v2</artifactId>
    <version>0.7.1</version>
</dependency>
```

```yaml
# 自定义配置（非 Spring 标准）
clickhouse:
  url: http://localhost:8123
  database: demo_db
  username: default
  password: clickhouse123
```

---

## 4. 进阶要点 / Advanced Topics

- **批量写入**：ClickHouse 不适合单行插入，务必批量写入（`JdbcTemplate.batchUpdate()` 或原生 Client 的 `insert()`）
- **分区策略**：按日期分区 `PARTITION BY toYYYYMM(event_time)`，加速查询和数据清理
- **物化视图**：预计算聚合结果，查询时直接读取
- **TTL 数据过期**：`TTL event_time + INTERVAL 90 DAY`，自动清理历史数据
- **MyBatis 集成**：ClickHouse JDBC 兼容 MyBatis，可用 XML Mapper 写复杂查询
- **与 Kafka 集成**：`Kafka` 表引擎直接消费 Kafka Topic
- **数据同步**：MySQL → ClickHouse 用 `MaterializedMySQL` 引擎或 Canal + 批量写入

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| 单行 INSERT 性能差 | 必须批量写入，建议每批 1000-10000 行 |
| 不支持 UPDATE/DELETE | 使用 `ReplacingMergeTree` + `FINAL` 或 `ALTER TABLE ... DELETE` |
| JDBC 连接超时 | ClickHouse 默认 HTTP 端口 8123，原生端口 9000 |
| JPA 不兼容 | ClickHouse 不适合 JPA，用 JdbcTemplate 或 MyBatis |
| 查询慢 | 检查是否命中分区、是否全表扫描 |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-clickhouse-demo/`](../../examples/spring-clickhouse-demo/)

包含：JDBC 方式 + 原生 Client 方式、批量写入、聚合查询、分区表示例。

启动依赖：
```bash
cd devops && docker compose -f clickhouse-compose.yml up -d
```

## 7. 参考链接 / References

- [ClickHouse 官方文档](https://clickhouse.com/docs)
- [ClickHouse JDBC Driver](https://github.com/ClickHouse/clickhouse-java)
- [ClickHouse 表引擎](https://clickhouse.com/docs/en/engines/table-engines)
