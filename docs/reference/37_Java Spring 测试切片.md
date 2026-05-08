---
title: Java Spring 测试切片
created: 2026-05-08 22:29:19
category: Java-Spring
tags:
  - Java
  - Spring
  - 测试
  - 测试切片
  - WebMvcTest
  - DataJpaTest
---

<!-- markdownlint-disable MD025 -->

# Java Spring 测试切片

## 为什么要学测试切片

前面我们学了单元测试（纯 Mockito，不启动容器）和集成测试（`@SpringBootTest`，启动完整容器）。这两种测试分别在"太快但太假"和"太真但太慢"两个极端。单元测试快但覆盖不了组件协作；`@SpringBootTest` 全面但启动一次 10-30 秒，而且加载 200 个 Bean 只为了测试一个 Controller 方法，太浪费。

测试切片（Test Slices）是 Spring Boot 提供的折中方案：**只加载与应用某一部分相关的 Bean**，比如测试 Controller 时只加载 Web 层组件，不加载数据库相关的 Bean。它保留了集成测试的真实性（用到真实的 Spring 容器），又大幅缩短了启动时间。

## 核心概念

### 测试切片是什么

测试切片是 Spring Boot 提供的一组注解，每个注解只加载 ApplicationContext 中的一个"切片"（特定层次的 Bean），而非启动整个应用。Spring Boot 预定义了多个切片注解，覆盖 Controller、Repository、JSON、安全等层次。

**换个说法：** `@SpringBootTest` 就像把整栋楼的灯全打开，只为了看书桌上的一页纸。测试切片就是只开书桌上的台灯 — 够亮且省电。

### 为什么需要测试切片

**痛点场景：** 你只想测试 `UserController` 的请求参数校验。用 `@SpringBootTest` 需要加载所有 Bean — Service、Repository、DataSource、Redis 连接... 启动一次 20 秒，40 个测试跑下来要 15 分钟。而且 DataSource 连接失败、Redis 连接超时都会导致测试整体失败，尽管你根本不测数据库和缓存。

**设计动机：** 测试切片让你声明"我只需要 Web 层的 Bean"，Spring Boot 就只创建 DispatcherServlet、Controller、Validator 等必要组件。启动 3 秒，测试 1 分钟跑完。

### 没有测试切片会怎样

**困境：** 要么写纯单元测试（Mock 一切，快但覆盖率低），要么写全量集成测试（慢但可靠）。中间没有梯度。200 个 Controller 的集成测试跑一次 15 分钟，开发者不愿意跑，测试逐渐被忽略。

**有了测试切片之后：** 三层测试策略成形 — 单元测试（毫秒）→ 测试切片（秒级）→ 全量集成测试（分钟级）。大部分测试在切片层解决，全量集成测试只保留少量关键链路。

## 概念深入解释

### 常用测试切片注解

| 注解 | 加载的 Bean | 适用场景 |
|------|------------|----------|
| `@WebMvcTest` | Controller、ControllerAdvice、Filter、Validator 等 Web 层组件 | 测试 Controller 路由、参数校验、异常处理 |
| `@DataJpaTest` | Entity、Repository、DataSource、JPA 相关 | 测试 Repository 方法、JPA 映射 |
| `@JsonTest` | JSON 序列化/反序列化相关（Jackson） | 测试 JSON 字段映射、日期格式、自定义序列化器 |
| `@RestClientTest` | RestTemplate、RestClient 相关 | 测试对外部 API 的 HTTP 调用 |
| `@JdbcTest` | DataSource、JdbcTemplate | 测试纯 JDBC 操作 |

### @WebMvcTest 详解

```java
@WebMvcTest(UserController.class)
class UserControllerSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean           // 替代容器中的真实 Bean
    private UserService userService;

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"email\":\"test@test.com\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUserWhenIdExists() throws Exception {
        when(userService.getUser(1L))
            .thenReturn(new UserResponse(1L, "Alice"));

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Alice"));
    }
}
```

**`@WebMvcTest` 的关键机制：**

1. **只加载 Web 层 Bean：** Controller、`@ControllerAdvice`、`Validator`、`MessageConverter`、`Filter` 等。不加载 `@Service`、`@Repository`、`@Component`。
2. **`@MockBean` 替代缺失的 Bean：** 被 Controller 依赖的 Service 不会被加载，需要用 `@MockBean` 手动提供一个 Mock 替代。
3. **MockMvc 而非 TestRestTemplate：** `@WebMvcTest` 默认使用 `webEnvironment = MOCK`，不启动服务器，用 `MockMvc` 模拟 HTTP 请求。

### @DataJpaTest 详解

```java
@DataJpaTest
class UserRepositorySliceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindByEmail() {
        User user = new User("test@example.com", "Alice");
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
    }
}
```

**`@DataJpaTest` 的关键机制：**

1. **只加载 JPA 相关 Bean：** Entity、Repository、`DataSource`、`EntityManager` 等。不加载 Controller、Service。
2. **自动配置内存数据库：** 默认用 H2 等嵌入式数据库替代项目中配置的真实数据源。
3. **提供 TestEntityManager：** 比直接使用 Repository 更适合做测试数据准备（绕过了某些 Hibernate 缓存问题）。
4. **事务自动回滚：** 和 `@SpringBootTest` 一样，每个测试方法结束后回滚。

### @JsonTest 详解

```java
@JsonTest
class UserJsonSliceTest {

    @Autowired
    private JacksonTester<UserDTO> json;

    @Test
    void shouldSerializeToSnakeCase() throws Exception {
        UserDTO user = new UserDTO("Alice", "alice@example.com");

        assertThat(json.write(user))
            .hasJsonPathStringValue("@.user_name")   // JSON 字段名
            .extractingJsonPathStringValue("@.user_email")
            .isEqualTo("alice@example.com");
    }
}
```

### 测试切片的选择策略

```
要测试的内容               → 推荐注解
────────────────────────────────────────
Controller 路由、参数校验   → @WebMvcTest
Controller 异常处理         → @WebMvcTest
JPA Entity 映射             → @DataJpaTest
Repository 自定义查询       → @DataJpaTest
JSON 序列化/反序列化        → @JsonTest
HTTP 客户端调用外部 API     → @RestClientTest

多 Service 协作 + DB 操作  → @SpringBootTest (完整集成测试)
单 Service 逻辑             → 纯 Mockito (单元测试)
```

### 测试金字塔

```
          ┌─────────────┐
          │  E2E 测试    │  ← 少量，验证关键用户流程
          ├─────────────┤
          │ 集成测试     │  ← 适量，验证组件协作
          ├─────────────┤
          │ 测试切片     │  ← 较多，验证层次行为 ← 本节
          ├─────────────┤
          │ 单元测试     │  ← 最多，验证单个类逻辑
          └─────────────┘
```

测试切片位于金字塔的中间偏下位置，数量上应该多于集成测试但少于单元测试。

## 核心要点

1. **@WebMvcTest 只测 Web 层：** Controller 路由、参数校验、异常处理、HTTP 状态码。不测 Service 逻辑。
2. **@DataJpaTest 只测数据层：** Repository 方法、JPA 映射、查询是否生成正确的 SQL。
3. **用 @MockBean 填补切片缺少的依赖：** Controller 依赖的 Service 在 @WebMvcTest 中不存在，必须显式 Mock。
4. **测试切片启动快于 @SpringBootTest：** 只加载需要的 Bean，3-5 秒 vs 10-30 秒。
5. **三层策略各司其职：** 单元测试验证逻辑，切片测试验证层次行为，全量集成测试验证端到端链路。

## 常见误区

- **@WebMvcTest 中试图 @Autowired 一个 Service。** Service 不在 Web 切片范围内，启动会报 NoSuchBeanDefinitionException。正确的做法是用 `@MockBean` Mock 掉 Service。
- **@WebMvcTest 中用 TestRestTemplate 而非 MockMvc。** @WebMvcTest 默认不启动 HTTP 服务器（`webEnvironment = MOCK`），没有端口，必须用 MockMvc。如果你需要真实 HTTP 请求，改用 `@SpringBootTest(webEnvironment = RANDOM_PORT)`。
- **@DataJpaTest 中默认用内存数据库，SQL 语句在真实库上不兼容。** 如果 Repository 中有数据库特有语法（原生 SQL、存储过程、方言函数），需要用 `@AutoConfigureTestDatabase(replace = NONE)` 关掉自动替换，手动配置真实数据源或用 Testcontainers。
- **多个 @WebMvcTest 测试类分别指定不同的 Controller 类，导致测试数据相互干扰。** Spring Boot 会缓存 ApplicationContext，如果两个 @WebMvcTest 的配置相同会复用上下文。但如果使用了 `@MockBean`，缓存会失效，每次启动新上下文，测试变慢。尽量把同组的 Controller Mock 配置统一。
- **把单元测试能做到的事用切片测试替代。** 验证一个纯 POJO 的计算逻辑不需要启动 Spring 容器，纯 JUnit + Mockito 足矣。用切片测试验证业务逻辑是杀鸡用牛刀。

## 与其他概念的关联

- **前置：** [Java Spring 单元测试](./35_Java%20Spring%20单元测试.md) -- 单元测试是纯 Mockito，测试切片是带 Spring 容器的轻量级测试
- **前置：** [Java Spring 集成测试](./36_Java%20Spring%20集成测试.md) -- 测试切片是全量集成测试的轻量替代
- **前置：** [Java Spring Controller](./16_Java%20Spring%20Controller.md) -- @WebMvcTest 的核心测试对象
- **前置：** [Java Spring Repository](./25_Java%20Spring%20Repository.md) -- @DataJpaTest 的核心测试对象
- **并行：** [Java Spring 响应与 DTO](./18_Java%20Spring%20响应与%20DTO.md) -- @JsonTest 验证 DTO 的序列化规则
- **后续：** [Java Spring Security](./38_Java%20Spring%20Security.md) -- 安全配置测试有专门的 @WithMockUser/@WithAnonymousUser 工具
