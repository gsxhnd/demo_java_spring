# 快速开始 / Getting Started

> 从零开始运行你的第一个示例项目

## 1. 环境要求 / Prerequisites

| 工具 | 最低版本 | 用途 |
|---|---|---|
| Java (JDK) | 21 LTS | 编译运行 Spring Boot 应用 |
| Maven | 3.9+ | 项目构建 |
| Docker | 24+ | 运行中间件（MySQL、Redis 等） |
| Docker Compose | v2 | 编排多个中间件容器 |
| Git | 2.x | 克隆项目 |

验证环境：

```bash
java -version    # openjdk 21.x.x
mvn -version     # Apache Maven 3.9.x
docker --version # Docker version 24.x+
docker compose version  # Docker Compose version v2.x
```

---

## 2. 克隆项目 / Clone

```bash
git clone https://github.com/<your-org>/demo_java_spring.git
cd demo_java_spring
```

---

## 3. 项目结构 / Project Structure

```
demo_java_spring/
├── docs/                          ← 学习文档（你正在看的）
│   ├── core/                      ← 核心基础篇
│   ├── database/                  ← 数据库篇
│   ├── framework/                 ← 框架核心篇
│   ├── microservice/              ← 微服务篇
│   └── advanced/                  ← 进阶主题篇
│
├── examples/                      ← 示例项目（独立可运行）
│   ├── docker-compose/            ← 中间件 Docker Compose 文件
│   ├── spring-mysql-demo/         ← MySQL + JPA + MyBatis-Plus
│   ├── spring-redis-demo/         ← Redis + Spring Cache
│   ├── spring-mongodb-demo/       ← MongoDB
│   ├── spring-es-demo/            ← Elasticsearch
│   ├── spring-clickhouse-demo/    ← ClickHouse
│   └── spring-influxdb-demo/      ← InfluxDB
│
└── README.md
```

---

## 4. 运行第一个示例 / Run Your First Example

以 `spring-mysql-demo` 为例：

### 4.1 启动 MySQL

```bash
cd examples/docker-compose
docker compose -f mysql-compose.yml up -d
```

验证 MySQL 已就绪：

```bash
docker compose -f mysql-compose.yml ps
# 状态应为 healthy
```

### 4.2 运行 Spring Boot 应用

```bash
cd ../spring-mysql-demo
mvn spring-boot:run
```

### 4.3 验证

应用启动后，访问：

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 或直接访问业务接口（具体路径参考各项目 README）
```

### 4.4 停止

```bash
# 停止 Spring Boot：Ctrl+C

# 停止 MySQL 容器
cd ../docker-compose
docker compose -f mysql-compose.yml down
```

---

## 5. 启动所有中间件 / Start All Middleware

如果你想一次性启动所有数据库和中间件：

```bash
cd examples/docker-compose
docker compose -f full-stack-compose.yml up -d
```

包含的服务：

| 服务 | 端口 |
|---|---|
| MySQL 8.0 | 3306 |
| PostgreSQL 16 | 5432 |
| Redis 7 | 6379 |
| MongoDB 7 | 27017 |
| Elasticsearch 8.15 | 9200 |
| Kibana 8.15 | 5601 |
| ClickHouse | 8123, 9000 |
| InfluxDB 2.7 | 8086 |

也可以按需只启动部分服务：

```bash
# 只启动 MySQL 和 Redis
docker compose -f full-stack-compose.yml up -d mysql redis
```

---

## 6. 推荐学习路径 / Recommended Learning Path

```
核心基础                框架能力                  数据层
   │                      │                       │
   ▼                      ▼                       ▼
IoC & DI              Actuator               MySQL (JPA)
   │                      │                       │
   ▼                      ▼                       ▼
Spring MVC            Logging → AOP          Redis → MongoDB
   │                      │                       │
   ▼                      ▼                       ▼
自动配置               Testing               Elasticsearch
   │                      │                       │
   ▼                      ▼                       ▼
事务管理            Security + JWT          ClickHouse → InfluxDB
                          │
                          ▼
                    JPA 深入 → 缓存 → 调度 → 异步
                          │
                          ▼
                       微服务篇
                          │
                          ▼
                       进阶主题
```

建议按照以下顺序阅读：

1. **核心基础**：[IoC & DI](core/ioc-di.md) → [Spring MVC](core/spring-mvc.md) → [自动配置](core/auto-configuration.md) → [事务管理](core/transaction.md)
2. **入门框架**：[Actuator](framework/actuator.md) → [Logging](framework/logging.md) → [AOP](framework/aop.md) → [Testing](framework/testing.md)
3. **数据层**：[MySQL](database/mysql.md) → [Redis](database/redis.md) → [MongoDB](database/mongodb.md)
4. **安全**：[Spring Security + JWT](framework/security-jwt.md)
5. **框架进阶**：[JPA 深入](framework/jpa-advanced.md) → [缓存体系](framework/cache.md) → [任务调度](framework/scheduling.md) → [异步处理](framework/async.md)
6. **微服务**：[Gateway](microservice/gateway.md) → [服务发现](microservice/service-discovery.md) → [配置中心](microservice/config-center.md) → ...
7. **进阶数据库**：[Elasticsearch](database/elasticsearch.md) → [ClickHouse](database/clickhouse.md) → [InfluxDB](database/influxdb.md)
8. **进阶主题**：[WebSocket](advanced/websocket.md) → [Spring Batch](advanced/batch.md) → [WebFlux](advanced/webflux.md) → [Modulith](advanced/modulith.md)

---

## 7. 常见问题 / FAQ

### Q: 端口被占用怎么办？

```bash
# 查看占用端口的进程
lsof -i :3306
# 或修改 docker-compose 文件中的端口映射
```

### Q: Docker 容器启动失败？

```bash
# 查看容器日志
docker compose -f mysql-compose.yml logs -f

# 清理后重试
docker compose -f mysql-compose.yml down -v
docker compose -f mysql-compose.yml up -d
```

### Q: Maven 下载依赖太慢？

在 `~/.m2/settings.xml` 中配置阿里云镜像：

```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/central</url>
  </mirror>
</mirrors>
```

### Q: 示例项目的数据库连接信息在哪？

每个示例项目的 `src/main/resources/application.yml` 中配置了连接信息，默认与 Docker Compose 中的配置一致，无需额外修改。

---

## 8. 参考资料 / References

- [完整文档索引](README.md)
- [示例项目说明](../examples/README.md)
- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/reference/)
