# Testing — 单元测试 / Unit & Integration Testing

> JUnit 5 + Mockito + TestContainers + MockMvc

## 1. 概述 / Overview

测试是保证代码质量的关键。Spring Boot 提供了完整的测试支持。

### 测试金字塔

```
         ┌───────┐
         │  E2E  │        ← 端到端测试（少量，慢）
        ─┼───────┼─
        │Integration│     ← 集成测试（适量）
       ─┼───────────┼─
       │  Unit Tests  │   ← 单元测试（大量，快）
      ─┴──────────────┴─
```

### 测试工具矩阵

| 工具 | 用途 |
|---|---|
| **JUnit 5** | 测试框架（断言、生命周期、参数化测试） |
| **Mockito** | Mock 框架（模拟依赖） |
| **MockMvc** | Spring MVC 测试（不启动服务器测试 Controller） |
| **WebTestClient** | WebFlux 测试（响应式） |
| **TestContainers** | 用 Docker 启动真实中间件（MySQL、Redis 等） |
| **AssertJ** | 流式断言库（比 JUnit 断言更易读） |
| **@SpringBootTest** | 启动完整 Spring 上下文的集成测试 |
| **@DataJpaTest** | 仅加载 JPA 相关 Bean 的切片测试 |
| **@WebMvcTest** | 仅加载 Controller 层的切片测试 |

---

## 2. 核心概念 / Core Concepts

### 测试类型

| 类型 | 注解 | 加载范围 | 速度 |
|---|---|---|---|
| 单元测试 | 无 Spring 注解 | 不加载 Spring 上下文 | 极快 |
| 切片测试 | `@WebMvcTest` / `@DataJpaTest` | 只加载相关层 | 快 |
| 集成测试 | `@SpringBootTest` | 完整 Spring 上下文 | 慢 |

### JUnit 5 核心注解

| 注解 | 说明 |
|---|---|
| `@Test` | 标记测试方法 |
| `@BeforeEach` / `@AfterEach` | 每个测试前/后执行 |
| `@BeforeAll` / `@AfterAll` | 所有测试前/后执行（static） |
| `@DisplayName` | 测试显示名称 |
| `@Disabled` | 禁用测试 |
| `@ParameterizedTest` | 参数化测试 |
| `@Nested` | 嵌套测试类（分组） |
| `@Tag` | 标签（按标签运行测试） |

### Mockito 核心注解

| 注解 | 说明 |
|---|---|
| `@Mock` | 创建 Mock 对象 |
| `@InjectMocks` | 自动注入 Mock 到被测对象 |
| `@Spy` | 部分 Mock（真实方法 + 可 stub） |
| `@Captor` | 参数捕获器 |

### Spring Boot 测试注解

| 注解 | 说明 |
|---|---|
| `@SpringBootTest` | 完整集成测试 |
| `@WebMvcTest(XxxController.class)` | Controller 切片测试 |
| `@DataJpaTest` | JPA Repository 切片测试（内嵌 H2） |
| `@MockBean` | 在 Spring 上下文中替换 Bean 为 Mock |
| `@AutoConfigureMockMvc` | 自动配置 MockMvc |

---

## 3. 快速集成 / Quick Start

### Maven 依赖

- `spring-boot-starter-test` — 包含 JUnit 5、Mockito、AssertJ、MockMvc
- `org.testcontainers:mysql` / `redis` / `mongodb` — TestContainers 模块（可选）

### 测试配置

| 配置 | 说明 |
|---|---|
| `src/test/resources/application-test.yml` | 测试环境配置 |
| `@ActiveProfiles("test")` | 激活测试 Profile |
| `@Testcontainers` + `@Container` | 启动 Docker 容器 |

---

## 4. 进阶要点 / Advanced Topics

- **MockMvc**：不启动服务器测试 Controller，验证状态码、响应体、Header
- **TestContainers**：用 Docker 启动真实 MySQL/Redis/MongoDB，替代 H2 内存数据库
- **参数化测试**：`@ParameterizedTest` + `@ValueSource` / `@CsvSource` / `@MethodSource`
- **测试覆盖率**：JaCoCo 插件生成覆盖率报告
- **测试数据准备**：`@Sql` 注解执行 SQL 脚本，或 `TestEntityManager` 插入数据
- **异步测试**：`Awaitility` 库等待异步操作完成
- **Contract Testing**：Spring Cloud Contract 消费者驱动契约测试
- **测试命名规范**：`should_ReturnUser_When_IdExists()`，清晰表达测试意图

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| `@SpringBootTest` 太慢 | 用切片测试（`@WebMvcTest`、`@DataJpaTest`）替代 |
| Mock 注入失败 | 确认使用 `@MockBean`（Spring）而非 `@Mock`（Mockito） |
| TestContainers 启动失败 | 确认 Docker 已启动，Docker Socket 可访问 |
| 测试间数据污染 | `@Transactional` 自动回滚，或 `@DirtiesContext` 重建上下文 |
| H2 和 MySQL 语法不兼容 | 用 TestContainers 启动真实 MySQL |

---

## 6. 示例项目 / Example

测试代码集成在各示例项目的 `src/test/` 目录中。

## 7. 参考链接 / References

- [Spring Boot Testing 官方文档](https://docs.spring.io/spring-boot/reference/testing/)
- [JUnit 5 用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 官方文档](https://site.mockito.org/)
- [TestContainers 官方文档](https://testcontainers.com/)
