---
title: Java Spring 单元测试
created: 2026-05-08 22:29:19
category: Java-Spring
tags:
  - Java
  - Spring
  - JUnit
  - Mockito
  - 单元测试
  - 测试
---

<!-- markdownlint-disable MD025 -->

# Java Spring 单元测试

## 为什么要学单元测试

前六个 Part 我们一路从理解核心概念走到了能写完整的业务代码。Controller 处理请求，Service 处理逻辑，Repository 操作数据库 — 一个完整的请求链路通了。现在你可能会想：代码写好了，怎么保证它是正确的？

最直接的方式是启动应用，用 Postman 发请求看结果。但这种方式极其低效 — 每改一行代码就要重启、登录、构造测试数据、发请求、检查结果。随着代码量增长，这种手工验证的方式越来越不可行。单元测试是用代码验证代码，一次编写，反复执行，秒级反馈。它是保证代码质量的基础设施，也是后续集成测试、CI/CD 流水线的前提。

## 核心概念

### 单元测试是什么

单元测试是对软件中最小可测试单元（通常是一个类或一个方法）进行正确性验证的测试。它在隔离环境中运行 — 被测对象的外部依赖（数据库、网络、文件系统）被 Mock 对象替代，只验证本单元的逻辑是否正确。

**换个说法：** 单元测试就像对汽车每个零件的单独质检 — 在装到整车之前，先保证每个齿轮、每根弹簧自身是合格的。你不会为了测试方向盘是否转动顺畅，先把轮胎、发动机全装好再测试。

### 为什么需要单元测试

**痛点场景：** 你在 `OrderService` 里加了一个库存检查逻辑。验证它是否正确需要：启动应用 → 登录 → 准备一个订单 → 手动把某商品库存设为 0 → 提交订单 → 检查是否返回"库存不足"。这个过程耗时 2 分钟以上，而且每次改代码都要重复。

**设计动机：** 单元测试把验证压缩为一段自动执行的断言：

```java
@Test
void shouldRejectOrderWhenStockInsufficient() {
    when(stockRepo.getStock("SKU-001")).thenReturn(0);
    assertThrows(InsufficientStockException.class,
        () -> orderService.placeOrder(orderWith("SKU-001")));
}
```

不需要启动应用，不需要数据库，不需要手动操作。秒级运行，CI 自动执行。

### 没有单元测试会怎样

**困境：** 靠手工测试 / Postman 脚本验证。回归测试变成噩梦 — 改一个底层方法，你不知道哪些功能被影响，只能把相关功能全部手工测一遍。重构是危险操作，因为没有人敢保证不引入 bug。

**有了单元测试之后：** 重构有安全网 — 改完代码跑一次测试，绿色通过说明行为没有意外变化。编写测试的过程本身也倒逼你写更好设计的代码（可测性强的代码通常是低耦合、依赖可注入的）。

## 概念深入解释

### 测试框架：JUnit 5

JUnit 5 是 Java 单元测试的标准框架。核心注解：

| 注解 | 作用 |
|------|------|
| `@Test` | 标记测试方法 |
| `@BeforeEach` | 每个测试方法执行前运行 |
| `@AfterEach` | 每个测试方法执行后运行 |
| `@BeforeAll` | 所有测试方法执行前运行一次（必须 static） |
| `@DisplayName` | 给测试方法起一个人类可读的名称 |
| `@ParameterizedTest` | 参数化测试，用不同输入跑同一个测试 |

### Mock 框架：Mockito

Mockito 是 Java 最流行的 Mock 框架。它的核心作用是：**创建一个可控的假对象，替代真实依赖**。

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepo;    // 假的 Repository

    @Mock
    private StockService stockService;    // 假的库存服务

    @InjectMocks
    private OrderService orderService;    // 真实的被测对象，依赖自动注入 Mock

    @Test
    @DisplayName("库存不足时应抛出异常")
    void shouldThrowWhenStockInsufficient() {
        when(stockService.checkStock("SKU-001", 10))
            .thenReturn(false);            // 模拟"库存不足"

        assertThrows(InsufficientStockException.class,
            () -> orderService.placeOrder(/* ... */));
    }
}
```

**核心 API：**

| Mockito 操作 | 含义 |
|--------------|------|
| `when(x.method()).thenReturn(y)` | 定义 Mock 行为：调用 method 时返回 y |
| `when(x.method()).thenThrow(ex)` | 定义 Mock 行为：调用 method 时抛异常 |
| `verify(x).method()` | 验证 method 是否被调用过 |
| `verify(x, times(3)).method()` | 验证 method 被调用了 3 次 |
| `verifyNoMoreInteractions(x)` | 验证没有其他调用 |
| `@Captor ArgumentCaptor<T>` | 捕获传入 Mock 的参数，用于断言 |

### 测试命名策略

好的测试名称应该描述"被测行为 + 条件 + 期望结果"：

```
shouldDoWhat_whenCondition
或
givenCondition_whenAction_thenExpected
```

示例：`shouldRejectOrder_whenStockInsufficient`、`givenValidUser_whenLogin_thenReturnToken`

### 测试方法结构：AAA 模式

```java
@Test
void shouldCalculateDiscountForVipUser() {
    // Arrange（准备：构造数据和 Mock 行为）
    User vipUser = new User("vip");
    when(userService.getLevel(vipUser)).thenReturn("VIP");

    // Act（执行：调用被测方法）
    BigDecimal price = pricingService.calculate(vipUser, new BigDecimal("100"));

    // Assert（断言：验证结果）
    assertEquals(0, new BigDecimal("80").compareTo(price));
}
```

### Spring 上下文与纯单元测试的区分

纯单元测试**不需要 Spring 上下文** — 不启动容器，不创建 Bean，只测试一个类。这就是本节讨论的测试形式，用 Mockito 替代依赖。它和集成测试的区别：

| 维度 | 纯单元测试 | 集成测试 (@SpringBootTest) |
|------|-----------|---------------------------|
| Spring 容器 | 不启动 | 启动完整容器 |
| 依赖处理 | Mockito 模拟 | 真实 Bean 注入 |
| 数据库 | 不连接 | 可选内存库或真实库 |
| 执行速度 | 毫秒级 | 秒级 |
| 测试目标 | 单个类的逻辑 | 多个类协作 |
| 可靠性 | 高（无外部依赖） | 中（依赖容器状态） |

### Mock vs Stub vs Fake

- **Mock：** 记录调用并验证行为。`verify(orderRepo).save(any())` — 关心"save 被调用了"
- **Stub：** 返回预设值。`when(repo.findById(1L)).thenReturn(product)` — 关心"返回什么"
- **Fake：** 一个简化的真实实现（如内存数据库替代真实数据库）

实际项目中以 Mock 和 Stub 为主。

## 核心要点

1. **单元测试不启动 Spring 容器：** 纯 Mockito + JUnit 5，毫秒级速度。需要容器启动的是集成测试。
2. **一个测试只验证一个行为：** 不要在一个测试方法里期望 5 件事 — 失败时不知道哪个错了。
3. **用 `@Mock` + `@InjectMocks` 管理依赖：** Mock 依赖，注入被测对象，清晰分离。
4. **测试命名表达意图：** `shouldThrow_whenStockInsufficient` 比 `testPlaceOrder` 好十倍。
5. **AAA 结构：** Arrange（准备）→ Act（执行）→ Assert（断言），结构统一易读。
6. **边界条件必须覆盖：** 正常值、null、空集合、超大值、负数，这些是最容易出 bug 的地方。

## 常见误区

- **启动 Spring 上下文跑"单元测试"，每个测试要 3 秒。** 如果测试里用了 `@SpringBootTest` 但只测一个 Service 方法，这不是单元测试，是用集成测试做单元测试的事。正确做法是纯 Mockito，不允许 Spring 容器启动。
- **Mock 了被测试对象本身。** `@InjectMocks` 标注的是真实的被测对象，`@Mock` 标注的是它的依赖。如果被测对象也被 Mock 了，你测的是 Mockito 的行为，不是你的代码。
- **`verify` 过度使用导致测试脆弱。** 每次改代码实现细节（如从调 `save` 改为调 `saveAndFlush`）都要改测试，说明测试和实现耦合太紧。优先用 `assertXxx` 验证返回值和状态，`verify` 用于验证关键副作用（如发邮件、写日志）。
- **忘记 `@ExtendWith(MockitoExtension.class)` 导致 Mock 注解不生效。** Mockito 需要 JUnit 5 扩展来初始化 `@Mock` 和 `@InjectMocks`。忘记加时，Mock 对象都是 null。
- **测试方法之间共享可变状态。** 如果 `@BeforeEach` 中没有重置 Mock 或共享字段，前一个测试的副作用会影响后一个测试的结果，表现为"单独跑通过，一起跑失败"。
- **测试了框架的能力而不是自己的逻辑。** 写测试验证 Spring Data JPA 的 `findById` 能查到数据 — 这不是你的逻辑，是框架已经测试过的。你应该 Mock 掉 `findById`，只测你拿到数据后的处理逻辑。

## 与其他概念的关联

- **前置：** [Java Spring Service 层](./30_Java%20Spring%20Service%20层.md) -- Service 是单元测试最主要的目标
- **前置：** [Java Spring DI](./07_Java%20Spring%20DI.md) -- 构造器注入让 Mock 替换依赖变得简单
- **并行：** [Java Spring 集成测试](./36_Java%20Spring%20集成测试.md) -- 单元测试测试单个类，集成测试测试多个类协作
- **并行：** [Java Spring 测试切片](./37_Java%20Spring%20测试切片.md) -- 测试切片是介于单元和集成之间的折中方案
- **后续：** [Java Spring Security](./38_Java%20Spring%20Security.md) -- 测试安全配置需要特殊的测试技巧
