# 学习指南

## 学习路线总览

```
Part 1 准备阶段 → Part 2 核心概念 → Part 3 Spring Boot 起步 → Part 4 Web 开发
     → Part 5 数据访问 → Part 6 业务能力 → Part 7 测试 → Part 8 安全
     → Part 9 通信协议 → Part 10 微服务 → Part 11 部署与进阶
```

每个 Part 有对应的实例项目，按编号顺序学习。详细学习路线参考 [reference/01_Java Spring.md](../reference/01_Java%20Spring.md)。

---

## Part 1：准备阶段

掌握 Spring 依赖的 Java 语言特性。

**实例项目**：`examples/01-spring-java-basics-demo`

**学习重点**：

- Java 注解机制 → [参考文档](../reference/02_Java%20注解机制.md)
- Java 反射基础 → [参考文档](../reference/03_Java%20反射基础.md)
- Lambda 与函数式接口 → [参考文档](../reference/04_Java%20Lambda%20与函数式接口.md)

```bash
cd examples/01-spring-java-basics-demo && mvn compile && mvn exec:java
```

---

## Part 2：核心概念

理解 Spring 框架核心机制，这是后续一切的基础。

**实例项目**：`examples/02-spring-core-demo`

**学习重点**：

- IoC 控制反转 → [参考文档](../reference/05_Java%20Spring%20IoC.md)
- Bean 定义与生命周期 → [参考文档](../reference/06_Java%20Spring%20Bean.md)
- DI 依赖注入 → [参考文档](../reference/07_Java%20Spring%20DI.md)
- AOP 面向切面编程 → [参考文档](../reference/08_Java%20Spring%20AOP.md)
- Spring 容器 → [参考文档](../reference/09_Java%20Spring%20容器.md)

```bash
cd examples/02-spring-core-demo && mvn spring-boot:run
```

---

## Part 3：Spring Boot 起步

让项目跑起来，并具备生产级可观测性。

**实例项目**：

- `examples/03-spring-boot-demo` — 基础启动
- `examples/04-spring-boot-observability-demo` — 可观测性增强（基于 03 扩展）

**学习重点**：

- Spring Boot 概述 → [参考文档](../reference/10_Java%20Spring%20Boot%20概述.md)
- 自动配置原理 → [参考文档](../reference/11_Java%20Spring%20Boot%20自动配置.md)
- Starter 机制 → [参考文档](../reference/12_Java%20Spring%20Boot%20Starter.md)
- 项目结构 → [参考文档](../reference/13_Java%20Spring%20Boot%20项目结构.md)
- 配置文件 → [参考文档](../reference/14_Java%20Spring%20Boot%20配置.md)
- 可观测性 → [参考文档](../reference/15_Java%20Spring%20可观测性.md)

```bash
# 基础启动
cd examples/03-spring-boot-demo && mvn spring-boot:run

# 可观测性版本
cd examples/04-spring-boot-observability-demo && mvn spring-boot:run
# 验证：curl http://localhost:8080/actuator/health
```

**项目关系说明**：04 是 03 的复制扩展版本。先学习 03 理解 Spring Boot 基础启动流程，再通过 04 学习如何为应用添加 Actuator、OpenTelemetry、Micrometer 和结构化日志。

---

## Part 4：Web 开发

写出第一个完整的 RESTful API。

**实例项目**：`examples/05-spring-web-demo`

**学习重点**：

- Controller 与路由 → [参考文档](../reference/16_Java%20Spring%20Controller.md)
- 请求参数处理 → [参考文档](../reference/17_Java%20Spring%20请求处理.md)
- 响应与 DTO → [参考文档](../reference/18_Java%20Spring%20响应与%20DTO.md)
- 异常处理 → [参考文档](../reference/19_Java%20Spring%20异常处理.md)
- 参数校验 → [参考文档](../reference/20_Java%20Spring%20参数校验.md)
- OpenAPI 文档 → [参考文档](../reference/21_Java%20Spring%20OpenAPI.md)

```bash
cd examples/05-spring-web-demo && mvn spring-boot:run
# Swagger UI: http://localhost:8080/swagger-ui.html
```

---

## 后续学习路径（Part 5-11）

待 Part 1-4 完成后逐步展开：

5. **数据访问**：MySQL (JPA) → MySQL (MyBatis) → Redis → MongoDB
6. **业务能力**：Service 层 → 配置管理 → 定时任务 → 缓存
7. **测试**：单元测试 → 集成测试 → 测试切片
8. **安全**：Spring Security + JWT
9. **通信协议**：WebSocket → MQTT → Modbus
10. **微服务**：Gateway → 服务发现 → 配置中心 → Feign → 熔断 → 分布式事务 → MQ
11. **部署与进阶**：Docker → K8s → WebFlux → Batch → Modulith

---

## 进阶技巧

### 调试示例项目

```bash
# 启用 Debug 日志（在 application.yml 中）
# logging.level.com.example: DEBUG

# 远程调试
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

IDE 中配置 Remote JVM Debug，连接到 `localhost:5005`。

### 同时运行多个示例项目

```bash
# 终端 1（默认端口 8080）
cd examples/05-spring-web-demo
mvn spring-boot:run

# 终端 2（指定端口 8081）
cd examples/04-spring-boot-observability-demo
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### 创建自己的示例项目

遵循项目约定：

1. 使用 `spring-boot-starter-parent:4.0.5` 作为 parent
2. Java 版本设为 21
3. 包结构：`com.example.{topic}/` 下分 `controller/`、`service/`、`config/` 等
4. 目录名格式：`{两位数序号}-spring-{topic}-demo`
5. 放入 `examples/` 目录
6. 使用与 Docker Compose 一致的连接配置

---

## 下一步

- 遇到问题 → [故障排查](./06-troubleshooting.md)
- 有疑问 → [常见问题](./07-faq.md)
- 查阅技术细节 → [技术参考文档](../reference/README.md)
