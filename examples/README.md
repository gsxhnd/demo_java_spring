# 示例项目

每个子目录是一个**独立的、可编译运行的** Spring Boot 项目，无交叉依赖。

## 项目索引

| 序号 | 项目 | 说明 | 中间件 | 启动命令 |
|------|------|------|--------|----------|
| 01 | [spring-java-basics-demo](./01-spring-java-basics-demo/) | Java 语言特性（注解、反射、Lambda） | 无 | `mvn exec:java` |
| 02 | [spring-core-demo](./02-spring-core-demo/) | Spring IoC / Bean / DI / AOP | 无 | `mvn exec:java` |
| 03 | [spring-boot-demo](./03-spring-boot-demo/) | Spring Boot 自动配置与起步 | 无 | `mvn spring-boot:run` |
| 04 | [spring-boot-observability-demo](./04-spring-boot-observability-demo/) | Actuator / Micrometer / OpenTelemetry | 无 | `mvn spring-boot:run` |
| 05 | [spring-web-demo](./05-spring-web-demo/) | RESTful API / DTO / 异常处理 / 校验 / OpenAPI | 无 | `mvn spring-boot:run` |
| 06 | [spring-data-jpa-demo](./06-spring-data-jpa-demo/) | Spring Data JPA + 读写分离双数据源 | MySQL | `mvn spring-boot:run` |
| 07 | [spring-data-mybatis-demo](./07-spring-data-mybatis-demo/) | MyBatis 注解 + XML 动态 SQL | MySQL | `mvn spring-boot:run` |
| 08 | [spring-multi-database-demo](./08-spring-multi-database-demo/) | Redis / MongoDB / ES / InfluxDB | Redis, MongoDB, ES, InfluxDB | `mvn spring-boot:run` |
| 09 | [spring-business-demo](./09-spring-business-demo/) | Service 层 / 缓存 / 异步 / 定时 / 文件 | MySQL, Redis | `mvn spring-boot:run` |
| 10 | [spring-testing-demo](./10-spring-testing-demo/) | 单元测试 / 集成测试 / 测试切片 (18 tests) | H2 | `mvn test` |
| 11 | [spring-security-demo](./11-spring-security-demo/) | Spring Security + JWT + Casbin RBAC | H2 | `mvn spring-boot:run` |
| 12 | [spring-communication-demo](./12-spring-communication-demo/) | WebSocket / MQTT / Modbus | (MQTT 可选: mosquitto) | `mvn spring-boot:run` |

## 项目约定

- **运行端口**：默认 8080（可修改）
- **配置格式**：YAML（`application.yml`）
- **包结构**：`com.example.{topic}/` → `controller/` `service/` `repository/` `entity/` `dto/` `config/` `exception/`
- **入口类**：`{Topic}DemoApplication.java`
- **注入方式**：构造器注入 + `private final` 字段
- **版本基线**：Java 21 + Spring Boot 4.0.5 + Maven 3.9+

## 快速启动

```bash
# 启动中间件（按需）
docker compose -f devops/full-stack-compose.yml up -d mysql redis

# 运行项目（以 05 为例）
cd examples/05-spring-web-demo
mvn spring-boot:run
```
