---
title: Java Spring DI
created: 2026-05-04 22:20:00
category: Java-Spring
tags:
  - Java
  - Spring
  - DI
  - 依赖注入
---

<!-- markdownlint-disable MD025 -->

# Java Spring DI（依赖注入）

## 为什么要学 DI

前面讲了 IoC 是原则，Bean 是被管理的对象。那 Bean 之间的依赖关系是怎么建立的呢？答案是 DI（Dependency Injection，依赖注入）。

DI 是 IoC 最主流的具体实现方式，也是 Spring 日常开发中最频繁使用的机制。每个 `@Autowired`、每个构造器参数、每个 `@Value` 注入配置值，都是在做依赖注入。不理解 DI，你就只能用 Spring 的一半能力。

---

## 核心概念

### DI 是什么

**DI（Dependency Injection，依赖注入）是一种设计模式：容器在创建 Bean 时，自动把它需要的依赖"注入"进去，而不是让 Bean 自己去查找或创建依赖。**

用代码对比最直观：

```java
// 没有 DI — 对象自己创建依赖
public class UserController {
    private UserService service = new UserServiceImpl();
}

// 有了 DI — 依赖由外部注入
public class UserController {
    private final UserService service;
    public UserController(UserService service) {
        this.service = service;
    }
}
```

有 DI 的版本中，`UserController` 不关心 `UserService` 是怎么创建的、是谁的实现。它只声明"我需要一个 UserService"，具体的创建和注入由 Spring 容器完成。

### 为什么需要 DI

**DI 实现了组件之间的"最小知识原则"。** 一个类不应该知道它的依赖是什么具体类、怎么创建的、什么时候创建的。它只需要知道依赖的接口。这带来可替换性（换实现不改调用方）、可测试性（注入 Mock 对象）、可读性（依赖关系显式化）。

### 没有 DI 会怎样

你需要在代码中硬编码 `new ServiceImpl()`，组件直接依赖具体类。换数据库要改遍所有调用方，测试时必须连接真实数据库。有了 DI，依赖关系由容器管理，业务代码只面向接口编程。

---

## 概念深入解释

### 三种注入方式

**构造器注入（推荐）** -- 依赖通过构造器参数传入：

```java
@Service
public class UserService {
    private final UserRepository repository;
    private final EmailService emailService;

    public UserService(UserRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }
}
```

推荐理由：
- 依赖不可变（可以用 `final` 修饰）
- 状态完备（构造完成后对象就是完整可用的）
- 显式依赖（构造器签名直接揭示所有依赖）
- 不需要 Spring 注解（纯 POJO，脱离 Spring 也能手动测试）。

**Setter 注入** -- 依赖通过 setter 方法注入，适用于可选依赖或运行时需要重新配置的场景。缺点是依赖可能未初始化，对象状态不完整。

```java
@Service
public class UserService {
    private UserRepository repository;

    @Autowired
    public void setRepository(UserRepository repository) {
        this.repository = repository;
    }
}
```

适用于可选依赖或运行时需要重新配置的场景。缺点是依赖可能未初始化（setter 没调用时），对象状态不完整。

**字段注入（不推荐）** -- 直接在字段上用 `@Autowired`。虽然简单，但依赖被隐藏在类内部、不能加 `final`、脱离 Spring 容器后无法测试、容易无意间注入过多依赖导致上帝类。

### 当有多个同类型 Bean 时

如果一个接口有多个实现，Spring 不知道注入哪一个：

```java
public interface PaymentService { }

@Service
public class AlipayService implements PaymentService { }

@Service
public class WechatPayService implements PaymentService { }

@Service
public class OrderService {
    // 两个 PaymentService 的实现类，Spring 无法决策
    @Autowired
    private PaymentService paymentService; // 报错
}
```

解决方案：

**@Qualifier** -- 指定 Bean 名称：

```java
@Autowired
@Qualifier("alipayService")
private PaymentService paymentService;
```

**@Primary** -- 标记默认实现：

```java
@Service
@Primary
public class AlipayService implements PaymentService { }
```

**集合注入** -- 注入所有实现：

```java
@Autowired
private List<PaymentService> paymentServices; // 注入全部实现类
```

### @Autowired 的底层机制

Spring 处理 `@Autowired` 时大致经历以下步骤：

1. 在容器中查找匹配类型的所有候选 Bean
2. 如果只有一个候选，直接注入
3. 如果有多个候选，优先考虑 `@Primary`、`@Priority`、`@Qualifier` 等限定信息
4. 如果仍有多个候选，再尝试按注入点名称（字段名或参数名）匹配 Bean 名称
5. 如果找不到候选且 `required = true`（默认），启动时报 `NoSuchBeanDefinitionException`
6. 最终通过构造器调用、Setter 调用或字段反射完成注入

### @Value 注入配置值

除了注入 Bean，DI 还支持注入配置值：

```java
@Value("${app.name}")
private String appName;

@Value("${server.port:8080}")  // 带默认值
private int port;
```

`@Value` 本质上也是一种依赖注入 -- 只不过注入的不是另一个 Bean，而是一个配置值。

---

## 核心要点

1. **DI 是 IoC 的具体实现。** IoC 说"由容器管理依赖"，DI 回答了"容器具体怎么做"。
2. **构造器注入是首选。** 依赖不可变、状态完备、脱离 Spring 也能用。
3. **避免字段注入。** 虽然方便，但在实际项目中埋坑 -- 不可测试、隐藏依赖、鼓励上帝类。
4. **遇到多个同类型 Bean 时用 @Primary 或 @Qualifier。**
5. **DI 不止注入 Bean，还注入配置值（@Value）。**

---

## 常见误区

- **把所有依赖都通过 DI 注入。** DI 用于注入"需要生命周期管理"的组件（Service、Repository、外部客户端等）。简单的值对象（DTO、VO）、纯工具类不需要注入。
- **循环依赖。** A 依赖 B，B 依赖 A。构造器注入的循环依赖无法创建对象，Spring 会直接报错；字段注入或 Setter 注入在部分单例场景下可以通过提前暴露引用解决，但 Spring Boot 2.6 起默认禁止循环依赖。出现循环依赖通常是设计问题，应该引入第三个类或重新设计接口。
- **在构造器里使用注入的依赖做初始化逻辑。** 构造器执行时依赖已经注入完成（构造器注入的依赖是通过参数传入的），但如果你在构造器里调用依赖的方法，而该依赖本身还没完全初始化，可能出问题。初始化逻辑应该放在 `@PostConstruct` 方法中。
- **用 `@Autowired` 注入 static 字段。** static 字段属于类，不属于实例。Spring 管理的是实例，不会直接注入 static 字段。更合理的做法是改成实例字段或实例方法，避免把容器管理的依赖塞进全局状态。

---

## 与其他概念的关联

- **前置：** [Java Spring IoC](./05_Java%20Spring%20IoC.md) -- DI 是 IoC 的实现方式，建议先理解 IoC 的思想。[Java Spring Bean](./06_Java%20Spring%20Bean.md) -- DI 注入的"依赖"和被注入的"目标"都是 Bean。[Java 反射基础](./03_Java%20反射基础.md) -- DI 的底层实现依赖反射（调用构造器、设置 private 字段）。
- **并行：** [Java Spring AOP](./08_Java%20Spring%20AOP.md) -- DI 注入的其实也是代理 Bean（如果该类有 AOP 切面的话）。
- **后续：** [Java Spring 容器](./09_Java%20Spring%20容器.md) -- 容器是执行 DI 的主体，理解 DI 后可以深入容器的内部运转。
