# 示例项目 / Example Projects

> 每个示例项目都是独立可运行的 Spring Boot 项目（目标版本 4.0.5 + Java 21，当前代码暂为 3.5.0 + Java 17，后续升级）

## 项目列表

| 项目 | 数据库/中间件 | 核心技术 | 状态 |
|---|---|---|---|
| [spring-mysql-demo](spring-mysql-demo/) | MySQL | JPA + MyBatis-Plus | 已创建 |
| [spring-redis-demo](spring-redis-demo/) | Redis | Spring Data Redis, Cache, 分布式锁 | 已创建 |
| [spring-mongodb-demo](spring-mongodb-demo/) | MongoDB | Spring Data MongoDB, 聚合管道 | 已创建 |
| [spring-es-demo](spring-es-demo/) | Elasticsearch | Spring Data ES, 全文搜索, 聚合 | 已创建 |
| [spring-clickhouse-demo](spring-clickhouse-demo/) | ClickHouse | JDBC, 批量写入, 聚合查询 | 已创建 |
| [spring-influxdb-demo](spring-influxdb-demo/) | InfluxDB | InfluxDB Client, Flux 查询 | 已创建 |
| spring-security-demo | - | Spring Security + JWT | 待生成 |
| spring-microservice-demo | 多中间件 | 微服务综合示例（多模块） | 待生成 |

## Docker Compose

所有中间件的 Docker Compose 文件位于 [docker-compose/](docker-compose/) 目录：

| 文件 | 服务 | 端口 |
|---|---|---|
| mysql-compose.yml | MySQL 8.0 | 3306 |
| postgresql-compose.yml | PostgreSQL 16 | 5432 |
| redis-compose.yml | Redis 7 | 6379 |
| mongodb-compose.yml | MongoDB 7 | 27017 |
| elasticsearch-compose.yml | ES 8.15 + Kibana | 9200, 5601 |
| clickhouse-compose.yml | ClickHouse | 8123, 9000 |
| influxdb-compose.yml | InfluxDB 2.7 | 8086 |
| full-stack-compose.yml | 以上全部 | 所有端口 |

### 使用方式

```bash
# 启动单个中间件
cd docker-compose
docker compose -f mysql-compose.yml up -d

# 启动所有中间件
docker compose -f full-stack-compose.yml up -d

# 按需启动（只启动 MySQL 和 Redis）
docker compose -f full-stack-compose.yml up -d mysql redis

# 停止
docker compose -f full-stack-compose.yml down
```

## 运行示例项目

```bash
# 1. 先启动对应的中间件
cd docker-compose && docker compose -f mysql-compose.yml up -d && cd ..

# 2. 运行示例项目
cd spring-mysql-demo
mvn spring-boot:run
```

## 通用说明

- 目标版本：Spring Boot 4.0.5 + Java 21（当前代码暂为 3.5.0 + Java 17，后续升级）
- 使用 Maven 构建
- 默认端口 8080（可在 application.yml 中修改）
- 中间件连接信息与 Docker Compose 中的配置一致
