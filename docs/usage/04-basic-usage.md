# 基础使用

> Spring 生态学习项目的核心使用方法

## 按推荐路径学习

本项目提供了结构化的学习路径，建议按以下顺序学习：

### 1. 核心基础篇

掌握 Spring 框架基础概念。

**学习重点**：
- IoC 容器和依赖注入的使用方式
- Spring MVC 的 RESTful API 开发
- Spring Boot 自动配置原理
- `@Transactional` 事务管理

**对应示例**：
```bash
cd examples/spring-ioc-demo && mvn spring-boot:run
cd examples/spring-mvc-demo && mvn spring-boot:run
cd examples/spring-autoconfig-demo && mvn spring-boot:run
cd examples/spring-transaction-demo && mvn spring-boot:run
```

### 2. 数据库篇

学习常用数据库在 Spring 中的集成方式。

**学习重点**：
- JPA 基础和 MyBatis 使用
- Redis 缓存和分布式锁
- MongoDB 文档存储
- Elasticsearch 全文搜索

**对应示例**：
```bash
# 启动 MySQL
cd examples/docker-compose && docker compose -f mysql-compose.yml up -d
cd ../spring-mysql-demo && mvn spring-boot:run

# 启动 Redis
cd ../docker-compose && docker compose -f redis-compose.yml up -d
cd ../spring-redis-demo && mvn spring-boot:run
```

### 3. 框架核心篇

深入 Spring 框架体系能力。

**学习重点**：
- Spring Security + JWT 认证授权
- AOP 面向切面编程
- Spring Boot Actuator 监控运维
- 单元测试和集成测试

**对应示例**：
```bash
cd examples/spring-security-demo && mvn spring-boot:run
cd examples/spring-cache-demo && mvn spring-boot:run
cd examples/spring-async-demo && mvn spring-boot:run
```

### 4. 微服务篇

构建分布式系统。

**学习重点**：
- Spring Cloud Gateway 网关配置
- Nacos 服务注册与发现
- OpenFeign 服务间调用
- Resilience4j 熔断降级

**对应示例**（待生成）：
```bash
cd examples/spring-microservice-demo
# 多模块 Maven 项目，详见项目内 README
```

### 5. 进阶主题篇

掌握高级和特定场景能力。

**学习重点**：
- WebSocket 实时通信
- Spring Batch 批处理
- Spring WebFlux 响应式编程
- Docker 容器化部署

**对应示例**：
```bash
cd examples/spring-websocket-demo && mvn spring-boot:run
cd examples/spring-batch-demo && mvn spring-boot:run
cd examples/spring-webflux-demo && mvn spring-boot:run
```

## 推荐学习顺序

1. **核心基础**：IoC & DI → Spring MVC → 自动配置与 Starter → 事务管理
2. **入门框架**：Actuator → Logging → AOP → Testing
3. **数据层**：MySQL (JPA) → MySQL (MyBatis) → Redis → MongoDB
4. **安全**：Spring Security + JWT
5. **框架进阶**：JPA 深入 → 缓存体系 → 任务调度 → 异步处理 → 数据校验 → 文件上传/下载
6. **微服务**：Gateway → Service Discovery → Config Center → OpenFeign → Circuit Breaker → MQ → OpenTelemetry → Seata
7. **进阶数据库**：Elasticsearch → ClickHouse → InfluxDB
8. **进阶主题**：WebSocket/SSE → Spring Batch → WebFlux → Spring Modulith → Docker 部署

## 文档查阅技巧

- 每个主题的索引页面（`README.md`）汇总了该主题的所有子文档
- 文档支持中英对照，技术术语保持英文
- 每个文档包含：核心概念 + 配置示例 + 代码片段 + 注意事项
- 详细技术参考文档位于 [reference/](../reference/00-readme.md) 目录下

## 下一步

掌握基础学习路径后，请阅读 [进阶使用](./05-advanced-usage.md) 了解高级技巧。
