# 配置说明

## 应用配置

每个示例项目的配置文件位于 `src/main/resources/application.yml`，格式为 YAML，项目已包含默认配置。

```yaml
# 示例：spring-mysql-demo 的 application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo_db
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8080
```

## Docker Compose 中间件配置

中间件配置集中在 `devops/` 目录：

| 文件 | 服务 | 端口 |
|------|------|------|
| `mysql-compose.yml` | MySQL 8.0 | 3306 |
| `postgresql-compose.yml` | PostgreSQL 16 | 5432 |
| `redis-compose.yml` | Redis 7 | 6379 |
| `mongodb-compose.yml` | MongoDB 7 | 27017 |
| `elasticsearch-compose.yml` | Elasticsearch 8.15 + Kibana | 9200, 5601 |
| `clickhouse-compose.yml` | ClickHouse | 8123, 9000 |
| `influxdb-compose.yml` | InfluxDB 2.7 | 8086 |
| `full-stack-compose.yml` | 以上全部 | 所有端口 |

### 环境变量

Docker Compose 中的关键环境变量（可在 compose 文件中修改）：

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `MYSQL_ROOT_PASSWORD` | `root123` | MySQL root 密码 |
| `MYSQL_DATABASE` | `demo_db` | 默认数据库 |
| `REDIS_PASSWORD` | （无） | Redis 密码（开发环境无密码） |
| `MONGO_INITDB_ROOT_USERNAME` | `admin` | MongoDB 管理员用户名 |
| `MONGO_INITDB_ROOT_PASSWORD` | `admin123` | MongoDB 管理员密码 |

## 端口规划

所有示例项目默认运行在 8080 端口。同时运行多个项目时需修改端口：

```yaml
server:
  port: 8081  # 改为其他端口
```

或通过命令行参数：

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

## 下一步

配置完成后，请阅读 [学习指南](./05-learning-guide.md) 开始学习。
