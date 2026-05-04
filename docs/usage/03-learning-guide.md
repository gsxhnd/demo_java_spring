# 学习指南

## 推荐学习路径

### 1. 核心基础篇

掌握 Spring 框架基础概念。

**学习重点**：IoC 容器和依赖注入、Spring MVC RESTful API 开发、Spring Boot 自动配置原理、`@Transactional` 事务管理

```bash
cd examples/spring-ioc-demo && mvn spring-boot:run
cd examples/spring-mvc-demo && mvn spring-boot:run
cd examples/spring-autoconfig-demo && mvn spring-boot:run
cd examples/spring-transaction-demo && mvn spring-boot:run
```

### 2. 数据库篇

学习常用数据库在 Spring 中的集成方式。

**学习重点**：JPA 基础和 MyBatis 使用、Redis 缓存和分布式锁、MongoDB 文档存储、Elasticsearch 全文搜索

```bash
# 启动 MySQL + Redis
docker compose -f devops/full-stack-compose.yml up -d mysql redis

cd examples/spring-mysql-demo && mvn spring-boot:run
cd examples/spring-redis-demo && mvn spring-boot:run
```

### 3. 框架核心篇

深入 Spring 框架体系能力。

**学习重点**：Spring Security + JWT 认证授权、AOP 面向切面编程、Spring Boot Actuator 监控运维、单元测试和集成测试

```bash
cd examples/spring-security-demo && mvn spring-boot:run
cd examples/spring-cache-demo && mvn spring-boot:run
```

### 4. 微服务篇

构建分布式系统。

**学习重点**：Spring Cloud Gateway 网关配置、Nacos 服务注册与发现、OpenFeign 服务间调用、Resilience4j 熔断降级

### 5. 进阶主题篇

掌握高级和特定场景能力。

**学习重点**：WebSocket 实时通信、Spring Batch 批处理、Spring WebFlux 响应式编程、Docker 容器化部署

## 详细学习顺序

1. **核心基础**：IoC & DI → Spring MVC → 自动配置与 Starter → 事务管理
2. **入门框架**：Actuator → Logging → AOP → Testing
3. **数据层**：MySQL (JPA) → MySQL (MyBatis) → Redis → MongoDB
4. **安全**：Spring Security + JWT
5. **框架进阶**：JPA 深入 → 缓存体系 → 任务调度 → 异步处理 → 数据校验 → 文件上传/下载
6. **微服务**：Gateway → Service Discovery → Config Center → OpenFeign → Circuit Breaker → MQ → OpenTelemetry → Seata
7. **进阶数据库**：Elasticsearch → ClickHouse → InfluxDB
8. **进阶主题**：WebSocket/SSE → Spring Batch → WebFlux → Spring Modulith → Docker 部署

## 文档查阅技巧

- 每个主题的 `README.md` 索引页汇总了该主题的所有子文档
- 每个文档包含：核心概念 + 配置示例 + 代码片段 + 注意事项
- 详细技术参考文档位于 [reference/](../reference/README.md) 目录下

## 进阶技巧

### 调试示例项目

```bash
# 启用 Debug 日志
# 在 application.yml 中添加：
# logging.level.com.example: DEBUG

# 远程调试
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

IDE 中配置 Remote JVM Debug，连接到 `localhost:5005`。

### 同时运行多个示例项目

```bash
# 终端 1 - MySQL 示例（端口 8080）
cd examples/spring-mysql-demo
mvn spring-boot:run

# 终端 2 - Redis 示例（端口 8081）
cd examples/spring-redis-demo
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### 创建自己的示例项目

遵循项目约定：

1. 使用 `spring-boot-starter-parent:4.0.5` 作为 parent
2. Java 版本设为 21
3. 包结构：`com.example.{topic}/` 下分 `controller/`、`service/`、`repository/` 等
4. 使用与 Docker Compose 一致的连接配置
5. 放入 `examples/` 目录

### 容器化部署示例项目

```bash
cd examples/spring-mysql-demo

# 构建镜像
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=spring-mysql-demo

# 运行容器
docker run -p 8080:8080 --network host spring-mysql-demo
```

## 下一步

- 遇到问题 → [故障排查](./04-troubleshooting.md)
- 有疑问 → [常见问题](./05-faq.md)
- 查阅技术细节 → [技术参考文档](../reference/README.md)
