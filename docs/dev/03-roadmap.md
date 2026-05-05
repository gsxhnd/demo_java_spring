# 开发路线图

## 概述

Spring 生态学习项目按照学习路线分为多个阶段（Part），每个阶段有对应的实例项目。
项目按学习顺序编号，放置在 `examples/` 目录下。

---

## Part 1 — 准备阶段

**目标**：掌握 Spring 依赖的 Java 语言特性

**实例项目**：`01-spring-java-basics-demo`

**覆盖内容**：

- Java 注解机制（自定义注解、元注解、注解处理）
- Java 反射基础（Class 对象、动态实例化、方法调用）
- Lambda 与函数式接口（Function/Consumer/Supplier、方法引用）

**验收标准**：

- 项目独立编译通过
- 包含注解定义与读取、反射操作、Lambda 使用的示例代码

**状态**：⏳ 待开始

---

## Part 2 — 核心概念

**目标**：理解 Spring 框架核心机制

**实例项目**：`02-spring-core-demo`

**覆盖内容**：

- IoC（控制反转）原理
- Bean 定义、作用域、生命周期
- DI（依赖注入）三种方式，构造器注入为主
- AOP（面向切面编程）
- ApplicationContext 容器

**验收标准**：

- 项目独立编译通过
- 演示构造器注入、Bean 生命周期回调、AOP 切面拦截

**状态**：⏳ 待开始

---

## Part 3 — Spring Boot 起步

**目标**：让项目跑起来，并具备可观测性

**实例项目**：

- `03-spring-boot-demo` — 基础启动实例
- `04-spring-boot-observability-demo` — 在 03 基础上添加可观测性

**覆盖内容**：

03-spring-boot-demo：

- Spring Boot 自动配置原理
- Starter 机制
- 项目结构与约定
- 配置文件（application.yml、Profile）

04-spring-boot-observability-demo（复制 03 后扩展）：

- Spring Boot Actuator（健康检查、指标端点）
- OpenTelemetry 集成（Traces、Metrics）
- Micrometer 指标
- 结构化日志（SLF4J + Logback JSON）

**验收标准**：

- 03 项目 `mvn spring-boot:run` 正常启动
- 04 项目启动后 `/actuator/health` 可访问
- 04 项目日志输出为 JSON 格式，包含 Trace ID

**状态**：⏳ 待开始

---

## Part 4 — Web 开发

**目标**：掌握 Spring MVC RESTful API 开发全流程

**实例项目**：`05-spring-web-demo`

**覆盖内容**：

- Controller 与路由（@RestController、@RequestMapping）
- 请求参数处理（@PathVariable、@RequestParam、@RequestBody）
- 响应处理与 DTO 设计
- 全局异常处理（@ControllerAdvice）
- 参数校验（Bean Validation）
- API 文档（springdoc-openapi / Swagger UI）

**验收标准**：

- 项目独立编译通过
- 提供完整的 CRUD API 示例
- Swagger UI 可访问并展示所有接口
- 参数校验失败返回统一错误格式

**状态**：⏳ 待开始

---

## 里程碑总览

| 阶段 | 目标 | 实例项目 | 状态 |
|------|------|----------|------|
| Part 1 | 准备阶段 | `01-spring-java-basics-demo` | ⏳ 待开始 |
| Part 2 | 核心概念 | `02-spring-core-demo` | ⏳ 待开始 |
| Part 3 | Spring Boot 起步 | `03-spring-boot-demo` + `04-spring-boot-observability-demo` | ⏳ 待开始 |
| Part 4 | Web 开发 | `05-spring-web-demo` | ⏳ 待开始 |

---

## 后续阶段（规划中）

以下阶段对应 `reference/01_Java Spring.md` 中的 Part 5-11，待前 4 个阶段完成后再规划实例项目：

- Part 5 — 数据访问（MySQL/JPA/MyBatis/Redis/MongoDB）
- Part 6 — 业务能力（Service 层/配置管理/定时任务/缓存/文件处理）
- Part 7 — 测试（单元测试/集成测试/测试切片）
- Part 8 — 安全（Spring Security/JWT）
- Part 9 — 通信协议（WebSocket/MQTT/Modbus）
- Part 10 — 微服务（Spring Cloud 全家桶）
- Part 11 — 部署与进阶（Docker/K8s/WebFlux/Batch）

---

## 项目命名规范

- 目录名格式：`{两位数序号}-spring-{topic}-demo`
- GroupId：`com.example`
- ArtifactId：与目录名一致
- 版本：`0.0.1-SNAPSHOT`
- Parent：`spring-boot-starter-parent:4.0.5`
- Java：21

---

## 测试要求

- 每个示例项目需包含基础单元测试
- 数据库/中间件相关示例包含集成测试
- 后续阶段完成后全项目测试覆盖率 ≥ 60%
