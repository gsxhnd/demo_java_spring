---
title: Java Spring Service 层
created: 2026-05-08 22:29:19
category: Java-Spring
tags:
  - Java
  - Spring
  - Service
  - 分层架构
---

<!-- markdownlint-disable MD025 -->

# Java Spring Service 层

## 为什么要学 Service 层

上一节我们完成了数据库访问 — 通过 Repository 能查数据，通过 Entity 定义了表结构，通过 `@Transactional` 管理了事务。现在 Controller 可以直接调用 Repository 了，表面上看似乎已经能干活了。但问题是：**Controller 里应该写多少逻辑？** 一个下单操作涉及库存检查、价格计算、优惠券核销、订单创建、扣减积分... 把这些全部塞进 Controller 方法里，这个方法会变成几百行的意大利面条。如果把逻辑放在 Repository 里，它又混入了数据库访问代码，职责不清。

这就是 Service 层存在的理由：在 Controller（入口）和 Repository（数据）之间建立一个纯粹的业务逻辑层。它不关心 HTTP 请求怎么解析，也不关心 SQL 怎么写，只关心**业务规则是什么**。

## 核心概念

### Service 层是什么

Service 层是 MVC 分层架构中负责封装业务逻辑的中间层。它从 Controller 接收已解析的输入数据，协调多个 Repository 或其他 Service 完成业务操作，将结果返回给 Controller。

**换个说法：** Controller 是前台接待，负责接电话、记录需求、返回结果；Repository 是仓库管理员，负责存取货物；Service 是业务经理，负责决定"这个订单能不能下、库存够不够、价格对不对"。前台不需要知道业务细节，仓库不需要知道业务规则。

### 为什么需要 Service 层

**痛点场景：** 假设你有一个电商系统，下单逻辑分散在 Controller 和 Repository 之间。有一天你需要在移动端 API 复用这个逻辑，或者需要在上线前加一个风控检查，你会发现逻辑散落在各处，改一处可能影响多处。

**设计动机：** Service 层提供单一职责的业务封装：
- Controller 只管 HTTP 协议转换（接收请求、返回响应）
- Repository 只管数据持久化
- Service 只管业务规则

当这三层各司其职时，更换任何一层（比如把 Controller 从 REST 改为 gRPC，或把 Repository 从 JPA 换为 MyBatis）都不会影响其他层。

### 没有 Service 层会怎样

**困境：** Controller 方法里混杂了参数校验、业务判断、数据库操作、异常处理、日志记录... 100 行一个方法很正常。同一个业务逻辑无法复用 — 当 REST API 和后台任务都需要下单能力时，只能复制粘贴。单元测试也几乎无法编写，因为业务代码和 HTTP 层紧紧绑在一起。

**有了 Service 层之后：** Controller 瘦身到 10 行以内（接收参数 → 调用 Service → 返回结果）。业务逻辑集中在一处，REST API、RPC 接口、消息消费者、定时任务都可以复用同一个 Service。测试 Service 不需要启动 Web 服务器，纯 Java 对象即可。

## 概念深入解释

### Service 层在 Spring 中的体现

Spring 通过 `@Service` 注解将类标记为 Service 组件（等价于 `@Component`，语义更明确）。Service 类通常是无状态的 — 所有方法调用不依赖实例变量，只依赖注入进来的其他 Bean。

```
HTTP 请求
    │
    ▼
Controller (@RestController)     ← 协议转换层
    │ @Autowired
    ▼
Service (@Service)                ← 业务逻辑层
    │ @Autowired
    ▼
Repository (@Repository / JpaRepository)  ← 数据访问层
    │
    ▼
Database
```

### Service 设计的核心原则

| 原则 | 说明 | 示例 |
|------|------|------|
| 无状态 | Service 不持有请求级别的数据，字段只能是注入的依赖 | `private final UserRepository repo;` -- 构造注入的依赖是唯一字段 |
| 单一职责 | 一个 Service 只负责一个领域 | `OrderService` 只做订单相关，不处理用户注册 |
| 依赖注入 | 通过构造器注入所需 Repository，不通过 `new` 创建 | 构造器 + `final` 字段 |
| 事务边界 | Service 方法通常是事务边界，标注 `@Transactional` | 一个业务操作 = 一个事务单元 |
| 调用链单向 | Controller → Service → Repository，不反向调用 | Service 不应依赖 Controller |

### Service 间的协作

一个 Service 可以调用另一个 Service。比如 `OrderService` 创建订单时需要 `CouponService` 核销优惠券：

```java
@Service
public class OrderService {
    private final OrderRepository orderRepo;
    private final CouponService couponService;

    public OrderService(OrderRepository orderRepo, CouponService couponService) {
        this.orderRepo = orderRepo;
        this.couponService = couponService;
    }

    @Transactional
    public OrderResult placeOrder(OrderRequest request) {
        couponService.validate(request.getCouponCode());
        // 业务逻辑继续...
        return orderRepo.save(order);
    }
}
```

### 常见反模式

**贫血 Service：** Service 只有一行 `return repo.save(entity)`，所有逻辑都在 Controller 里。这是把 Service 当透明管道用，失去了分层意义。

**胖 Service：** 一个 `UserService` 里有 200 个方法，包含了用户注册、权限管理、积分计算等所有用户相关逻辑。应该按子域拆分：`UserRegistrationService`、`UserPermissionService`、`UserPointsService`。

**Service 中有 Web 层依赖：** `myService.getRequestIp()` 之类的写法，Service 直接依赖 HttpServletRequest — 破坏了分层，使其无法在非 Web 上下文复用。

## 核心要点

1. **Service 是纯粹的业务逻辑层：** 不接触 HTTP 对象，不写 SQL，只表达业务规则。
2. **用 `@Service` + 构造器注入：** `@Service` 标记组件身份，构造器注入保证依赖不可变。
3. **Service 方法是事务边界：** 一个对外暴露的 Service 方法通常对应一个事务，标注 `@Transactional`。
4. **无状态设计：** Service 的字段只能是注入的其他 Bean，不能是请求级别的数据。
5. **Controller 只做代理：** Controller 方法不超过 10 行 — 接收参数、调用 Service、返回响应。
6. **按领域拆分 Service：** 不要让一个 Service 管所有事，按业务子域拆分为多个小 Service。

## 常见误区

- **把所有逻辑都塞进 Controller，Service 只做转发。** 这等于放弃了分层架构的复用性和可测试性。Controller 应该瘦到只做协议转换。
- **在 Service 里直接操作 HttpServletRequest 或 HttpSession。** Service 一旦依赖 Web 层对象，就无法在定时任务、消息队列消费者、单元测试中复用。正确的做法是在 Controller 层提取需要的信息，通过参数传入 Service。
- **一个 Service 方法里调用几十个 Repository 方法，但没有事务注解。** 每个 Repository 调用都是独立操作，部分成功部分失败时数据会不一致。在 Service 方法上标注 `@Transactional` 保证原子性。
- **把 DTO 校验逻辑写在 Service 里。** DTO 的格式校验应该通过 `@Valid` + Bean Validation 在 Controller 层完成。进入 Service 的数据应该是已经通过校验的。Service 做的是业务规则校验（如"库存是否足够"），而不是格式校验（如"邮箱格式是否正确"）。
- **启动时报 NoSuchBeanDefinitionException 找不到 Service。** 通常是因为 Service 类没有 `@Service` 注解，或者类放在了 `@ComponentScan` 扫描范围之外。确认包结构在启动类所在包的子包内。

## 与其他概念的关联

- **前置：** [Java Spring Controller](./16_Java%20Spring%20Controller.md) -- Controller 是 Service 的调用方
- **前置：** [Java Spring Repository](./25_Java%20Spring%20Repository.md) -- Service 通过 Repository 访问数据
- **前置：** [Java Spring 事务管理](./26_Java%20Spring%20事务管理.md) -- Service 方法是事务边界，需要 `@Transactional`
- **并行：** [Java Spring 响应与 DTO](./18_Java%20Spring%20响应与%20DTO.md) -- DTO 在 Controller 和 Service 之间传递数据
- **并行：** [Java Spring 参数校验](./20_Java%20Spring%20参数校验.md) -- Controller 层校验输入，Service 层校验业务规则
- **后续：** [Java Spring 单元测试](./35_Java%20Spring%20单元测试.md) -- Service 是单元测试的核心测试对象
