---
title: Java Spring 集成测试
created: 2026-05-08 22:29:19
category: Java-Spring
tags:
  - Java
  - Spring
  - 集成测试
  - SpringBootTest
  - 测试
---

<!-- markdownlint-disable MD025 -->

# Java Spring 集成测试

## 为什么要学集成测试

上一节我们学了单元测试 — 用 Mockito 隔离依赖，单独测试一个 Service 的方法。单元测试快、隔离、可靠，但它有一个盲区：**Mock 的行为是你自己设定的，不代表真实环境就是那样**。你 Mock 了 `userRepo.findByEmail()` 返回一个用户，但真实数据库里这个 SQL 能不能执行？你 Mock 了 Redis 缓存返回 null，但真实的序列化和网络反序列化会不会出问题？

集成测试弥补这个差距。它启动真实的（或接近真实的）Spring 上下文，让组件按真实方式协作，验证的不是"单个零件是否合格"，而是"零件装在一起会不会出问题"。

## 核心概念

### 集成测试是什么

集成测试（Integration Test）是启动 Spring ApplicationContext（完整或部分），加载真实的 Bean、配置和基础设施，验证多个组件之间协作行为的测试。

**换个说法：** 单元测试是单独检验每个乐器的音准，集成测试是让整个管弦乐队合奏一次 — 检验的不是单件乐器好不好，而是它们配合起来是否和谐。

### 为什么需要集成测试

**痛点场景：** 你在 `UserService.register()` 中调用了 `userRepo.save(user)`。单元测试 Mock 了这个调用，验证了 `save` 被调用且参数正确。但现实是：
- `User` Entity 的 `@Column` 约束可能让 SQL insert 失败（字段长度不够、非空约束冲突）
- JPA 的级联配置（`@OneToMany(cascade = ...`) 可能产生意料之外的 delete 操作
- 事务回滚可能因为配置错误而不生效
- JSON 序列化配置可能导致 Controller 返回的字段名不对

这些问题是 Mock 永远发现不了的，因为它们只在真实协作时暴露。

### 没有集成测试会怎样

**困境：** "单元测试全绿，部署上去一跑就报错" — 这是典型的虚假信心。Mock 掩盖了持久化、序列化、事务、安全等环节的问题，这些问题恰恰是生产故障的常见来源。

**有了集成测试之后：** 用真实（或内存）数据库验证完整链路。启动时如果 Bean 配置有问题立即发现，不会留到部署后。

## 概念深入解释

### @SpringBootTest 核心配置

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldRegisterAndLogin() {
        // 注册
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        ResponseEntity<UserResponse> registerResp = restTemplate
            .postForEntity("/api/users/register", request, UserResponse.class);
        assertEquals(201, registerResp.getStatusCode().value());

        // 登录
        LoginRequest login = new LoginRequest("test@example.com", "password123");
        ResponseEntity<TokenResponse> loginResp = restTemplate
            .postForEntity("/api/auth/login", login, TokenResponse.class);
        assertEquals(200, loginResp.getStatusCode().value());
        assertNotNull(loginResp.getBody().getToken());
    }
}
```

**webEnvironment 选项：**

| 值 | 含义 | 适用场景 |
|----|------|----------|
| `MOCK`（默认） | 不启动真实 HTTP 服务器，Mock Servlet 环境 | 用 MockMvc 测试 Controller |
| `RANDOM_PORT` | 启动内嵌服务器，随机端口 | HTTP 客户端测试、真实请求 |
| `DEFINED_PORT` | 启动内嵌服务器，固定端口 | 需要固定端口时 |
| `NONE` | 不启动 Web 环境 | 纯 Service/Repository 集成测试 |

### TestRestTemplate vs MockMvc

| 方式 | 机制 | 优点 | 缺点 |
|------|------|------|------|
| `MockMvc` | 模拟 HTTP 请求，不经过网络 | 快速、无需端口 | 不测试真实的序列化/反序列化完整链路 |
| `TestRestTemplate` | 真实 HTTP 请求 | 测试完整的网络协议链路 | 稍慢、需要端口 |

对于集成测试，`TestRestTemplate` 更接近真实环境。

### 事务与测试

Spring 测试框架的一个重要特性是 **测试事务自动回滚**：

```java
@SpringBootTest
@Transactional   // 每个测试方法结束后自动回滚
class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUser() {
        userService.register("test@test.com", "pass");
        // 数据库中确实插入了一行...
    }
    // ...但测试方法结束后自动回滚，数据不会持久化
}
```

这个行为确保测试之间不相互污染。如果需要不回滚（如测试事务提交行为），使用 `@Commit` 注解。

### 测试数据管理

集成测试需要测试数据，常见策略：

| 策略 | 方式 | 适用 |
|------|------|------|
| 每次测试前构造 | `@BeforeEach` 中用 Repository 插入 | 少量数据、简单场景 |
| SQL 初始化脚本 | `@Sql("/test-data.sql")` 在每个测试前执行 | 固定数据集 |
| DBUnit / Testcontainers | 用容器启动真实数据库 | 需要真实数据库方言的行为验证 |
| H2 内存数据库 | `@AutoConfigureTestDatabase` 替换真实数据源 | 快速但不支持数据库特有方言 |

### Testcontainers

对于需要真实数据库方言的集成测试（如 PostgreSQL 的 JSONB 类型、MySQL 的特定函数），H2 内存库不够。Testcontainers 用 Docker 启动真实数据库实例：

```java
@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

### 测试执行速度的分层策略

```
快速反馈 (< 1s)       单元测试 (Mockito)          写代码时频繁跑
    ↓
中等反馈 (5-30s)      测试切片 (@WebMvcTest等)     本地提交前跑
    ↓
慢速反馈 (1-5 min)    完整集成测试 (@SpringBootTest) CI 流水线中跑
    ↓
端到端验证 (10+ min)  E2E 测试 (Selenium等)       发版前跑
```

核心思想：把不同类型的测试放在不同阶段执行，保证开发时反馈足够快，提交时验证足够充分。

## 核心要点

1. **集成测试用 @SpringBootTest 启动上下文：** 测试 Bean 装配、配置加载、组件协作，而非单个类的逻辑。
2. **用 TestRestTemplate 做真实 HTTP 测试：** 比 MockMvc 更接近真实环境，验证完整的序列化和路由链路。
3. **测试事务默认回滚：** 集成测试不污染数据库，每个测试方法结束后自动回滚。
4. **用 Testcontainers 获得真实数据库行为：** 当项目依赖特定数据库方言时，用容器替代 H2 内存库。
5. **测试数据用 @Sql 或 @BeforeEach 准备：** 确保每个测试有干净、确定的数据集。
6. **集成测试跑得慢，分层管理：** 本地开发不频繁跑全量集成测试，交给 CI 流水线。

## 常见误区

- **把集成测试当单元测试用，测试单个 Service 方法。** 如果一个测试只需要 Mock 掉所有依赖就能测，那应该写成单元测试。集成测试的价值在于验证**多个真实组件的协作**和**基础设施的正确配置**。
- **集成测试中用 H2 内存库替代真实数据库，上线后 SQL 语法错误。** H2 兼容大部分标准 SQL，但不支持数据库特有语法（如 PostgreSQL 的 `INSERT ... ON CONFLICT`、MySQL 的 `ON DUPLICATE KEY`）。如果你用了这些特有语法，集成测试必须连真实数据库（Testcontainers）。
- **忘记测试事务自动回滚，手动删数据。** Spring 测试框架默认在每个 `@Test` 方法后回滚事务，不需要手工清理。除非用了 `@Commit` 或者异步逻辑（`@Async`/不同线程的事务不受管理），否则数据不会残留。
- **在一个测试方法里测了太多场景，失败时难以定位。** 集成测试同样遵循"一个测试一个场景"原则。一个测试方法链式调用七八个 API，中间任何一个失败，所有断言都失效。
- **Testcontainers 在 CI 环境中不可用导致测试跳过。** CI 环境需要 Docker daemon。如果 CI 不支持 Docker，用 `@EnabledIf` 条件注解跳过 Testcontainers 测试，同时保留 Spring 上下文基础的集成测试。

## 与其他概念的关联

- **前置：** [Java Spring 单元测试](./35_Java%20Spring%20单元测试.md) -- 集成测试建立在单元测试之上，验证单元测试无法覆盖的协作问题
- **前置：** [Java Spring 容器](./09_Java%20Spring%20容器.md) -- `@SpringBootTest` 启动完整的 ApplicationContext
- **前置：** [Java Spring Controller](./16_Java%20Spring%20Controller.md) -- TestRestTemplate 测试完整的 HTTP 请求链路
- **并行：** [Java Spring 测试切片](./37_Java%20Spring%20测试切片.md) -- 测试切片是轻量级集成测试
- **后续：** [Java Spring Security](./38_Java%20Spring%20Security.md) -- 安全配置需要集成测试验证 Filter Chain 是否正确
