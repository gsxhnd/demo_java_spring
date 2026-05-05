---
title: Java Spring
created: 2026-05-04 20:30:00
tags:
  - Java
  - Spring
  - Spring-Boot
  - 学习路线
---

<!-- markdownlint-disable MD025 -->

# Java Spring

## 写在前面

作为一个有开发经验的人，我对 Web 开发、数据库操作、API 设计这些概念并不陌生。但 Java Spring 生态对我来说是全新的领域。这篇文档是我学习 Spring 的起点 — 一份完整的学习路线图。

我不打算在这里写复杂的代码，代码实践会放在独立的项目中。这里只记录**概念、思路、和学习路径**。每个主题都会链接到对应的详细文档，我会在后续学习中逐步填充它们。

### 为什么是 Spring？

Spring 解决的核心问题是：**如何管理复杂应用中对象之间的依赖关系**。

在没有框架的情况下，你需要手动创建对象、管理它们的生命周期、处理它们之间的依赖。当应用变大，这些工作会变得极其繁琐且容易出错。Spring 通过"控制反转"的思想，把这些脏活累活接管了。

Spring Boot 则更进一步 — 它在 Spring 的基础上提供了开箱即用的体验，让你不需要写大量配置就能启动一个生产级应用。

---

## 学习路线总览

```
准备阶段 → 核心概念 → 跑起来（含可观测性） → 写 API → 连数据库 → 写业务
    → 写测试 → 加安全 → 通信协议 → 微服务 → 部署上线
```

下面是每个阶段的详细说明。

---

## Part 1：准备阶段

> 在进入 Spring 之前，有几个 Java 语言特性是必须理解的，因为 Spring 大量依赖它们。

### 1.1 为什么选择 Spring

Spring 不是唯一的 Java 框架，但它是生态最完整的。从 Web 开发到微服务，从消息队列到批处理，Spring 几乎覆盖了企业开发的所有场景。选择 Spring 意味着选择了一个成熟的、有大量社区支持的技术栈。

### 1.2 Java 注解机制

Spring 的配置几乎全部通过注解完成（`@Component`、`@Autowired`、`@RestController` 等）。理解注解是什么、怎么定义、怎么被框架读取，是学习 Spring 的前提。

**注解本质上是一种元数据** — 它不直接影响代码逻辑，但框架可以在运行时读取它们来决定行为。

→ [Java 注解机制](./02_Java%20注解机制.md)

### 1.3 Java 反射基础

Spring 在底层大量使用反射来实例化对象、调用方法、注入依赖。你不需要精通反射，但需要理解它能做什么：在运行时获取类的信息、创建实例、调用方法。

→ [Java 反射基础](./03_Java%20反射基础.md)

### 1.4 Java Lambda 与函数式接口

Spring 5+ 和 Spring WebFlux 中大量使用 Lambda 表达式。理解函数式接口（`Function`、`Consumer`、`Supplier`）和 Lambda 语法是必要的。

→ [Java Lambda 与函数式接口](./04_Java%20Lambda%20与函数式接口.md)

---

## Part 2：核心概念

> 这是整个 Spring 学习中最重要的部分。理解了这些概念，后面的一切都是应用。

### 2.1 什么是 IoC（控制反转）

**是什么：** IoC（Inversion of Control）是一种设计原则。传统方式下，对象自己创建和管理依赖；IoC 模式下，对象的依赖由外部容器提供。

**为什么需要它：** 当 A 依赖 B，B 依赖 C，C 依赖 D... 手动管理这些依赖链会让代码变得脆弱且难以测试。IoC 让你只需要声明"我需要什么"，容器负责"怎么给你"。

**解决什么问题：** 解耦。让组件之间不再硬编码依赖关系，使代码更容易测试、更容易替换实现。

→ [Java Spring IoC](./05_Java%20Spring%20IoC.md)

### 2.2 什么是 Bean

**是什么：** Bean 就是由 Spring 容器管理的对象。任何一个普通的 Java 对象，一旦交给 Spring 容器管理，它就是一个 Bean。

**为什么叫 Bean：** 这是 JavaBeans 规范的延续。在 Spring 语境下，Bean = 被容器管理的对象实例。

**生命周期：** Bean 有完整的生命周期 — 创建、初始化、使用、销毁。Spring 容器控制这一切。

**作用域：** 默认是 Singleton（整个应用只有一个实例），也可以是 Prototype（每次请求创建新实例）、Request、Session 等。

**目的：** 让你不需要自己 `new` 对象，容器帮你管理对象的创建和生命周期。

→ [Java Spring Bean](./06_Java%20Spring%20Bean.md)

### 2.3 什么是 DI（依赖注入）

**是什么：** DI（Dependency Injection）是 IoC 的具体实现方式。容器在创建 Bean 时，自动把它需要的依赖"注入"进去。

**三种注入方式：**

- 构造器注入（推荐）— 通过构造函数参数注入
- Setter 注入 — 通过 setter 方法注入
- 字段注入（不推荐）— 直接在字段上用 `@Autowired`

**为什么推荐构造器注入：** 依赖是显式的、不可变的（`final`），且对象创建后就是完整可用的状态。

→ [Java Spring DI](./07_Java%20Spring%20DI.md)

### 2.4 什么是 AOP（面向切面编程）

**是什么：** AOP（Aspect-Oriented Programming）允许你把横切关注点（如日志、事务、权限检查）从业务逻辑中分离出来。

**为什么需要它：** 假设你有 50 个方法都需要记录日志。没有 AOP，你需要在每个方法里写日志代码。有了 AOP，你只需要定义一次"在方法执行前后记录日志"的规则。

**核心术语：**

- Aspect（切面）— 横切关注点的模块化
- Pointcut（切入点）— 定义在哪些方法上生效
- Advice（通知）— 在切入点执行的具体逻辑（前置、后置、环绕）

→ [Java Spring AOP](./08_Java%20Spring%20AOP.md)

### 2.5 Spring 容器（ApplicationContext）

**是什么：** ApplicationContext 是 Spring 的核心容器，负责创建、配置和管理所有 Bean。

**它做了什么：** 读取配置（注解或 XML）→ 实例化 Bean → 解析依赖 → 注入依赖 → 管理生命周期。

**你可以把它理解为：** 一个智能的对象工厂，你告诉它需要什么，它负责生产和组装。

→ [Java Spring 容器](./09_Java%20Spring%20容器.md)

---

## Part 3：Spring Boot 起步

> 理解了核心概念后，是时候让项目跑起来了。

### 3.1 Spring vs Spring Boot

Spring 是基础框架，提供 IoC、AOP 等核心能力，但需要大量手动配置。

Spring Boot 是 Spring 的"脚手架"，提供：

- 自动配置（根据依赖自动配置 Bean）
- 内嵌服务器（不需要外部 Tomcat）
- Starter 依赖（一个依赖搞定一类功能）
- 生产就绪特性（健康检查、指标等）

简单说：Spring Boot 让你用 Spring 但不用操心配置。

→ [Java Spring Boot 概述](./10_Java%20Spring%20Boot%20概述.md)

### 3.2 自动配置原理

**是什么：** Spring Boot 根据 classpath 中的依赖，自动配置相应的 Bean。比如你引入了 `spring-boot-starter-web`，它会自动配置内嵌 Tomcat、DispatcherServlet 等。

**原理：** `@EnableAutoConfiguration` + 自动配置类索引 + 条件注解（`@ConditionalOnClass` 等）。Spring Boot 2 主要从 `META-INF/spring.factories` 加载自动配置，Spring Boot 3 改为 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。

**为什么重要：** 理解自动配置能帮你排查"为什么某个 Bean 没生效"或"为什么行为不符合预期"。

→ [Java Spring Boot 自动配置](./11_Java%20Spring%20Boot%20自动配置.md)

### 3.3 Starter 机制

**是什么：** Starter 是一组预定义的依赖集合。引入一个 Starter 就等于引入了一类功能所需的所有依赖。

**常用 Starter：**

- `spring-boot-starter-web` — Web 开发
- `spring-boot-starter-data-jpa` — 数据库访问
- `spring-boot-starter-security` — 安全
- `spring-boot-starter-test` — 测试

→ [Java Spring Boot Starter](./12_Java%20Spring%20Boot%20Starter.md)

### 3.4 项目结构与约定

Spring Boot 推荐按功能/领域组织代码，而不是按技术层分包。

```
com.example.app
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   └── UserRepository.java
├── order/
│   ├── OrderController.java
│   ├── OrderService.java
│   └── OrderRepository.java
└── Application.java
```

→ [Java Spring Boot 项目结构](./13_Java%20Spring%20Boot%20项目结构.md)

### 3.5 配置文件

Spring Boot 使用 `application.yml`（或 `.properties`）管理配置。支持多环境配置（Profile）、类型安全绑定（`@ConfigurationProperties`）、外部化配置。

→ [Java Spring Boot 配置](./14_Java%20Spring%20Boot%20配置.md)

### 3.6 可观测性（Actuator + OpenTelemetry + 日志）

应用跑起来之后，你需要知道它是否健康、请求经过了哪些环节、每步耗时多少、哪里出了问题。可观测性解决的就是这个问题，它由三大支柱组成：Traces（链路追踪）、Metrics（指标）、Logs（日志）。

**Actuator — 应用侧暴露入口：** Spring Boot Actuator 提供一组内置端点，例如健康检查（默认 Web 暴露 `/actuator/health`）、性能指标（`/actuator/metrics`）、环境配置（`/actuator/env`）、Bean 列表（`/actuator/beans`）等。默认情况下 Web 只暴露少量安全端点，`metrics`、`env`、`beans` 等通常需要通过 `management.endpoints.web.exposure.include` 显式开放。它是查看应用内部状态的重要入口。

**OpenTelemetry — 统一观测标准：** OpenTelemetry（OTel）定义了 Traces、Metrics、Logs 的采集和导出规范。Spring Boot 通常通过 Micrometer Observation、Micrometer Tracing、OTel Exporter 或 OTel Java Agent 采集和导出观测数据。Actuator 负责暴露应用内部状态和管理端点；OTel 负责统一采集、关联和导出遥测数据，两者不是简单的"Actuator 产出、OTel 发送"关系。

**三大支柱：**

- **Tracing（链路追踪）** — 追踪一个请求从入口到出口的完整路径。每个操作是一个 Span，多个 Span 组成一个 Trace。配合 Jaeger 或 Tempo 可视化
- **Metrics（指标）** — 用 Micrometer 暴露应用指标（QPS、延迟、错误率），对接 Prometheus + Grafana
- **Structured Logging（结构化日志）** — Spring Boot 默认使用 SLF4J + Logback。日志分级（TRACE、DEBUG、INFO、WARN、ERROR），结构化输出（JSON）便于集中收集和查询（ELK / Loki）。日志应包含 Trace ID 以关联链路追踪

→ [Java Spring 可观测性](./15_Java%20Spring%20可观测性.md)

---

## Part 4：Web 开发

> 能跑起来之后，下一步是写出第一个 API。

### 4.1 Controller 与路由

**Controller** 是处理 HTTP 请求的入口。用 `@RestController` 标注一个类，用 `@GetMapping`、`@PostMapping` 等标注方法，就定义了路由。

Spring 的 DispatcherServlet 负责把请求分发到对应的 Controller 方法。

→ [Java Spring Controller](./16_Java%20Spring%20Controller.md)

### 4.2 请求参数处理

Spring 提供了多种方式接收请求参数：

- `@PathVariable` — 路径参数（`/users/{id}`）
- `@RequestParam` — 查询参数（`?name=xxx`）
- `@RequestBody` — 请求体（JSON → 对象）
- `@RequestHeader` — 请求头

→ [Java Spring 请求处理](./17_Java%20Spring%20请求处理.md)

### 4.3 响应处理与 DTO

**DTO（Data Transfer Object）** 是专门用于 API 层数据传输的对象。不要直接把数据库 Entity 暴露给客户端 — 这会泄露内部结构，也会让 API 和数据库耦合。

→ [Java Spring 响应与 DTO](./18_Java%20Spring%20响应与%20DTO.md)

### 4.4 全局异常处理

用 `@ControllerAdvice` + `@ExceptionHandler` 实现统一的异常处理。所有 Controller 抛出的异常都会被拦截，转换为统一格式的错误响应。

→ [Java Spring 异常处理](./19_Java%20Spring%20异常处理.md)

### 4.5 参数校验（Bean Validation）

用 JSR 380 注解（`@NotNull`、`@Size`、`@Email` 等）在 DTO 上声明校验规则，配合 `@Valid` 自动触发校验。校验失败会抛出异常，被全局异常处理器捕获。

→ [Java Spring 参数校验](./20_Java%20Spring%20参数校验.md)

### 4.6 API 文档（OpenAPI / Swagger）

**OpenAPI** 是描述 RESTful API 的行业标准规范。**Swagger UI** 是基于 OpenAPI 规范自动生成的交互式 API 文档界面。

Spring Boot 通过 `springdoc-openapi` 库自动扫描 Controller 和 DTO 上的注解，生成 OpenAPI 3.0 规范文档，并提供 Swagger UI 供前端和测试人员直接调试接口。

**为什么需要：** 手动维护 API 文档容易过时。自动生成的文档始终与代码同步，减少前后端沟通成本。

→ [Java Spring OpenAPI](./21_Java%20Spring%20OpenAPI.md)

---

## Part 5：数据访问

> API 写好了，接下来连接数据库。

### 5.1 什么是 ORM 和 JPA

**ORM（Object-Relational Mapping）：** 把数据库表映射为 Java 对象，让你用面向对象的方式操作数据库，而不是写 SQL。

**JPA（Java Persistence API）：** Java 官方的 ORM 规范。Hibernate 是 JPA 最流行的实现。

**Spring Data JPA：** 在 JPA 之上再封装一层，让你只需要定义接口就能完成大部分数据库操作。

→ [Java Spring ORM 与 JPA](Java Spring ORM 与 JPA.md)

### 5.2 Spring Data JPA 基础

定义一个继承 `JpaRepository` 的接口，Spring 自动提供 CRUD 方法。你甚至可以通过方法命名约定自动生成查询（如 `findByUsername`）。

→ [Java Spring Data JPA](Java Spring Data JPA.md)

### 5.3 Entity 定义与关系映射

用 `@Entity` 标注类，用 `@Column`、`@Id`、`@OneToMany`、`@ManyToOne` 等注解定义表结构和关系。

→ [Java Spring Entity](Java Spring Entity.md)

### 5.4 Repository 接口与查询方法

除了方法命名查询，还可以用 `@Query` 写 JPQL 或原生 SQL，或者用 Specification 构建动态查询。

→ [Java Spring Repository](Java Spring Repository.md)

### 5.5 事务管理

**`@Transactional`** 声明式事务管理。标注在方法上，Spring 自动处理事务的开启、提交、回滚。

**关键点：** 事务传播行为、隔离级别、只读事务优化。

→ [Java Spring 事务管理](Java Spring 事务管理.md)

### 5.6 MyBatis 集成

**是什么：** MyBatis 是另一种流行的持久层框架，与 JPA 的"全自动 ORM"不同，MyBatis 是"半自动"的 -- 你写 SQL，它帮你做参数映射和结果映射。

**为什么需要它：** JPA 适合简单 CRUD 和标准查询，但在复杂 SQL（多表联查、动态条件、数据库特有语法）场景下，手写 SQL 更直接、更可控。很多项目会 JPA + MyBatis 混用。

**Spring 集成：** `mybatis-spring-boot-starter` 提供开箱即用的集成，支持注解 SQL 和 XML Mapper 两种方式。

→ [Java Spring MyBatis](Java Spring MyBatis.md)

### 5.7 多数据库实践

实际项目中经常需要同时连接多个数据库。不同数据库解决不同问题：

- **MySQL / PostgreSQL** — 关系型数据，Spring Data JPA 原生支持
- **Redis** — 缓存、会话、分布式锁，Spring Data Redis 集成
- **MongoDB** — 灵活 Schema、内容管理，Spring Data MongoDB 集成
- **Elasticsearch** — 全文搜索、日志分析，Spring Data Elasticsearch 集成
- **ClickHouse** — OLAP 分析型数据库，通过 JDBC 接入
- **InfluxDB** — 时序数据库，监控指标、IoT 传感器数据

**多数据源配置：** 如何在一个应用中配置多个 DataSource，每种数据库有不同的方言、特性和最佳实践。

→ [Java Spring 多数据库](Java Spring 多数据库.md)

---

## Part 6：业务能力

> 能连数据库后，需要学会写规范的业务代码。

### 6.1 Service 层设计

Service 层封装业务逻辑，是 Controller 和 Repository 之间的桥梁。Service 应该是无状态的，一个 Service 方法对应一个业务操作。

→ [Java Spring Service 层](Java Spring Service 层.md)

### 6.2 配置管理与 Profile

用 Profile 管理多环境配置（dev、test、prod）。用 `@ConfigurationProperties` 把配置绑定到类型安全的 Java 对象。

→ [Java Spring 配置管理](Java Spring 配置管理.md)

### 6.3 定时任务与异步处理

- `@Scheduled` — 定时任务
- `@Async` — 异步方法执行
- `@EnableScheduling` / `@EnableAsync` — 启用对应功能

→ [Java Spring 异步与定时任务](Java Spring 异步与定时任务.md)

### 6.4 缓存

**是什么：** Spring Cache 是一套缓存抽象，通过 `@Cacheable`、`@CacheEvict`、`@CachePut` 等注解声明式地管理缓存。

**为什么需要它：** 数据库查询是昂贵的操作。对于读多写少的数据（如配置、热门商品），缓存可以大幅降低数据库压力、提升响应速度。

**底层实现：** 可以对接多种缓存提供者 -- 本地缓存（Caffeine）、分布式缓存（Redis）。Spring 的缓存抽象让你切换实现时不需要改业务代码。

→ [Java Spring 缓存](Java Spring 缓存.md)

### 6.5 文件上传与下载

文件处理是 Web 应用的常见需求。Spring Boot 通过 `MultipartFile` 接收上传文件，通过 `Resource` 返回下载文件。

**关键点：** 文件大小限制配置、存储策略（本地磁盘 vs 对象存储）、流式处理大文件避免内存溢出。

→ [Java Spring 文件处理](Java Spring 文件处理.md)

---

## Part 7：测试

> 代码写完了要保证质量。测试是日常开发的一部分，越早养成习惯越好。

### 7.1 单元测试（JUnit 5 + Mockito）

测试单个类的行为，用 Mockito mock 掉外部依赖。快速、隔离、可重复。

→ [Java Spring 单元测试](Java Spring 单元测试.md)

### 7.2 集成测试（@SpringBootTest）

启动完整的 Spring 上下文，测试多个组件协作的行为。比单元测试慢，但更接近真实环境。

→ [Java Spring 集成测试](Java Spring 集成测试.md)

### 7.3 测试切片

Spring Boot 提供了"测试切片"注解，只加载应用的一部分：

- `@WebMvcTest` — 只测试 Controller 层
- `@DataJpaTest` — 只测试 Repository 层
- `@JsonTest` — 只测试 JSON 序列化

兼顾速度和真实性。

→ [Java Spring 测试切片](Java Spring 测试切片.md)

---

## Part 8：安全

> API 和数据库都通了，现在给它们加上保护。

### 8.1 Spring Security 概述

Spring Security 是一个强大且高度可定制的认证和授权框架。它通过 Filter Chain（过滤器链）拦截请求，在请求到达 Controller 之前完成安全检查。

**核心概念：**

- Authentication（认证）— 你是谁
- Authorization（授权）— 你能做什么
- SecurityFilterChain — 安全过滤器链

→ [Java Spring Security](Java Spring Security.md)

### 8.2 认证与授权

认证方式：表单登录、HTTP Basic、OAuth2、自定义认证。

授权方式：基于角色（ROLE_ADMIN）、基于权限（READ_USER）、方法级安全（`@PreAuthorize`）。

→ [Java Spring 认证与授权](Java Spring 认证与授权.md)

### 8.3 JWT 集成

在前后端分离架构中，JWT（JSON Web Token）是最常用的无状态认证方案。Spring Security + JWT 的集成是一个高频需求。

→ [Java Spring JWT](Java Spring JWT.md)

---

## Part 9：通信协议集成

> 在单体应用中掌握各种通信协议，为后续微服务打基础。

### 9.1 WebSocket 实时通信

**是什么：** WebSocket 是全双工通信协议，适合实时场景（聊天、通知、实时数据推送）。

**Spring 支持：** Spring WebSocket + STOMP 协议，提供消息代理、订阅/发布模式。

→ [Java Spring WebSocket](Java Spring WebSocket.md)

### 9.2 MQTT 消息协议

**是什么：** MQTT 是轻量级的发布/订阅消息协议，广泛用于 IoT 场景。

**Spring 支持：** Spring Integration MQTT 或 Eclipse Paho 客户端集成。适合设备数据上报、指令下发。

→ [Java Spring MQTT](Java Spring MQTT.md)

### 9.3 Modbus 工业协议

**是什么：** Modbus 是工业自动化领域的通信协议，用于 PLC、传感器等设备通信。

**Java 集成：** 通过 jlibmodbus、modbus4j 等库实现。Spring 中通常封装为 Service 层组件。

→ [Java Spring Modbus](Java Spring Modbus.md)

### 9.4 协议选型对比与场景分析

不同协议适用于不同场景：

- WebSocket — 浏览器实时通信
- MQTT — IoT 设备、低带宽环境
- Modbus — 工业设备直连
- HTTP/REST — 通用 API 调用
- gRPC — 高性能服务间通信

→ [Java Spring 通信协议选型](Java Spring 通信协议选型.md)

---

## Part 10：Spring Cloud 微服务

> 所有前置知识的综合应用。从单体走向分布式。

### 10.1 微服务架构概述

**是什么：** 把一个大应用拆分成多个小服务，每个服务独立部署、独立扩展。

**代价：** 分布式带来的复杂性 — 网络不可靠、数据一致性、服务发现、配置管理。

**Spring Cloud：** 一套解决微服务常见问题的工具集。

→ [Java Spring Cloud 概述](Java Spring Cloud 概述.md)

### 10.2 服务注册与发现

服务启动时注册自己，调用方通过注册中心找到目标服务。

**常用方案：** Nacos、Eureka、Consul。Nacos 在国内生态中更主流。

→ [Java Spring Cloud 服务发现](Java Spring Cloud 服务发现.md)

### 10.3 API 网关（Spring Cloud Gateway）

所有外部请求的统一入口。负责路由、限流、认证、日志等横切关注点。

→ [Java Spring Cloud Gateway](Java Spring Cloud Gateway.md)

### 10.4 配置中心

集中管理所有服务的配置，支持动态刷新。

**常用方案：** Nacos Config、Spring Cloud Config、Apollo。

→ [Java Spring Cloud 配置中心](Java Spring Cloud 配置中心.md)

### 10.5 服务间通信

- **Feign** — 声明式 HTTP 客户端，像调用本地方法一样调用远程服务
- **gRPC** — 高性能 RPC 框架，适合内部服务间通信
- **消息队列** — 异步解耦（RabbitMQ、Kafka）

→ [Java Spring Cloud 服务通信](Java Spring Cloud 服务通信.md)

### 10.6 熔断与限流

**是什么：** 熔断（Circuit Breaker）是一种保护机制 -- 当下游服务故障时，快速失败而不是无限等待，防止故障扩散（雪崩效应）。限流（Rate Limiting）则控制请求速率，保护服务不被流量压垮。

**为什么需要它：** 微服务架构中，一个服务的故障可能拖垮整条调用链。熔断器在检测到连续失败后"断开电路"，直接返回降级响应，给下游服务恢复的时间。

**常用方案：** Resilience4j（Spring Cloud 官方推荐）、Sentinel（阿里开源，功能更丰富，国内生态主流）。

→ [Java Spring Cloud 熔断限流](Java Spring Cloud 熔断限流.md)

### 10.7 分布式事务

**是什么：** 当一个业务操作跨越多个服务（每个服务有自己的数据库），如何保证数据一致性。这是微服务架构中最棘手的问题之一。

**为什么需要它：** 单体应用中一个 `@Transactional` 就能搞定的事，在微服务中变成了跨网络、跨数据库的协调问题。

**常用方案：** Seata（阿里开源，支持 AT/TCC/Saga 模式）、基于消息队列的最终一致性方案。大多数场景下优先考虑最终一致性，强一致性方案代价很高。

→ [Java Spring Cloud 分布式事务](Java Spring Cloud 分布式事务.md)

### 10.8 消息队列

**是什么：** 消息队列（Message Queue）是服务间异步通信的中间件。生产者发送消息到队列，消费者从队列拉取消息处理。

**为什么需要它：** 解耦服务间的直接依赖、削峰填谷（应对突发流量）、保证最终一致性。

**常用方案：**

- **RabbitMQ** — 功能丰富、支持复杂路由，适合业务消息
- **Kafka** — 高吞吐、持久化、适合日志收集和事件流

**Spring 集成：** Spring AMQP（RabbitMQ）、Spring Kafka，提供声明式的消息监听和发送。

→ [Java Spring Cloud 消息队列](Java Spring Cloud 消息队列.md)

---

## Part 11：部署与进阶

> 一切就绪，准备上线。

### 11.1 打包与运行

Spring Boot 应用打包为可执行 JAR（Fat JAR），内嵌服务器，`java -jar` 直接运行。也支持打包为 WAR 部署到外部容器。

→ [Java Spring 打包部署](Java Spring 打包部署.md)

### 11.2 Docker 容器化

编写 Dockerfile，构建镜像，容器化部署。Spring Boot 还提供了 Buildpacks 支持，无需 Dockerfile 也能构建镜像。

→ [Java Spring Docker](Java Spring Docker.md)

### 11.3 下一步方向

学完以上内容后，可以继续探索：

- **Spring WebFlux（响应式编程）** — 基于 Reactor 的非阻塞 Web 框架，适合高并发、IO 密集型场景。与传统 Spring MVC 是两套编程模型，学习曲线较陡。
- **Spring Batch（批处理）** — 企业级批处理框架，适合定期数据迁移、报表生成、ETL 等场景。提供 Job/Step/Reader/Writer 抽象。
- **Spring Modulith（模块化单体）** — 在单体应用中实现模块化架构，为未来可能的微服务拆分做准备。比直接上微服务更务实的渐进方案。
- **Spring Cloud Stream（事件驱动）** — 基于消息中间件的事件驱动微服务框架
- **GraalVM Native Image（原生编译）** — 将 Spring Boot 应用编译为原生可执行文件，启动时间从秒级降到毫秒级
- **Kubernetes 部署与编排** — 容器编排平台，管理微服务的部署、扩缩容、服务发现

→ [Java Spring 进阶方向](Java Spring 进阶方向.md)

---

## 推荐资源

| 类型 | 资源 | 说明 |
|------|------|------|
| 官方文档 | [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/) | 最权威的参考 |
| 官方指南 | [Spring Guides](https://spring.io/guides) | 官方 Getting Started 教程 |
| 书籍 | 《Spring in Action》 | 经典入门书 |
| 视频 | Baeldung | 高质量 Spring 教程网站 |
| 社区 | Stack Overflow `spring-boot` tag | 问题解答 |

---

## 总结

这份文档是我学习 Java Spring 的路线图。11 个阶段，从基础概念到微服务部署，每一步都有明确的目标。

我的学习策略是：

1. **先理解概念** — 在这份文档和详细文档中记录
2. **再动手实践** — 在独立项目中写代码
3. **最后分享总结** — 把学习过程整理成博客

接下来，我会按照这个路线逐步创建每个主题的详细文档。每完成一个阶段，回来更新这份概述的进度。

Let's go.
