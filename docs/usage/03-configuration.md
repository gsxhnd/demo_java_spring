# 配置说明

## 配置文件

- **位置**：每个示例项目的 `src/main/resources/application.yml`
- **格式**：YAML
- **创建方式**：项目已包含，无需手动创建

## 中间件配置

### Docker Compose 配置

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

## 应用配置

每个示例项目的 `application.yml` 包含对应中间件的连接信息，与 Docker Compose 配置保持一致：

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

## 端口规划

多个示例项目同时运行时需注意端口冲突。建议：

| 示例项目 | 默认端口 |
|----------|----------|
| spring-mvc-demo | 8080 |
| spring-mysql-demo | 8080 |
| spring-redis-demo | 8080 |

修改端口方式：

```yaml
server:
  port: 8081  # 改为其他端口
```

## 配置示例

完整的中间件启动配置：

```bash
# 1. 启动全部中间件
cd devops
docker compose -f full-stack-compose.yml up -d

# 2. 查看运行状态
docker compose -f full-stack-compose.yml ps

# 3. 运行示例项目
cd ../spring-mysql-demo
mvn spring-boot:run
```

## 下一步

配置完成后，请阅读 [基础使用](./04-basic-usage.md) 开始学习。
