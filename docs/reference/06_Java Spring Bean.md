---
title: Java Spring Bean
created: 2026-05-04 22:10:00
category: Java-Spring
tags:
  - Java
  - Spring
  - Bean
  - IoC
---

<!-- markdownlint-disable MD025 -->

# Java Spring Bean

## 为什么要学 Bean

上一节讲了 IoC 是把对象的控制权交给容器。那容器管理的这些"对象"在 Spring 里叫什么呢？叫 Bean。

Bean 是 Spring 世界里最核心的概念之一。Spring 的几乎一切操作都围绕 Bean 展开：创建 Bean、管理 Bean、注入 Bean、销毁 Bean。如果 IoC 是哲学，Bean 就是实体。

---

## 核心概念

### Bean 是什么

**在 Spring 的语境中，Bean 就是由 Spring IoC 容器管理的对象。**

这个定义有两个关键点：

1. **它是对象。** Bean 本质上就是一个 Java 对象实例，有字段、有方法。
2. **它被容器管理。** 不是你 `new` 出来的，而是 Spring 容器创建、配置、管理的。

任何一个普通的 Java 类，一旦它的实例由 Spring 容器创建并管理，它就是一个 Bean。除此之外并没有什么特殊之处 -- Bean 就是普通的 POJO。

"Bean" 这个名字来自 JavaBeans 规范。JavaBean 最初的定义是：一个可重用的软件组件，遵循特定命名约定（无参构造器、私有字段 + getter/setter、可序列化）。

Spring 借用了这个名字，但放宽了约束。在 Spring 中，Bean 不强制要求 getter/setter，也不强制要求实现 `Serializable`。任何被容器管理的对象都是 Bean。

### 为什么需要 Bean

Bean 是 IoC 的产出物和基本单元。Spring 的所有上层能力 -- 依赖注入、AOP 代理、事务管理、安全控制 -- 都作用在 Bean 上。只有被容器管理的对象才能享受这些能力。

### 没有 Bean 会怎样

你 `new` 出来的对象是"野生"的 -- Spring 不知道它的存在，不会注入依赖、不会应用 AOP、不会管理生命周期。你需要自己管理对象的创建顺序、单例/多例、初始化和资源释放。有了 Bean，这些全部由容器统一管理，你只需要声明"这个类需要被管理"。

---

## 概念深入解释

### Bean 的创建方式

**组件扫描（最常用）** -- 用 `@Component` 及其派生注解标记类，Spring 自动扫描并注册为 Bean：

```java
@Service  // 语义上更明确，效果和 @Component 完全一样
public class UserService { }
```

`@Component` 有四个语义化的派生注解：`@Service`、`@Repository`、`@Controller`、`@RestController`。它们在功能上等价，区别在于语义表达和可扩展性（`@Repository` 自动翻译持久层异常，`@Controller` 配合 MVC 使用）。

**@Bean 方法** -- 在 `@Configuration` 类中显式声明，适合创建第三方库的对象：

```java
@Configuration
public class AppConfig {
    @Bean
    public DataSource dataSource() {
        // 第三方库的类，无法加 @Component
        return new HikariDataSource(config);
    }
}
```

**XML 配置（旧式）** -- Spring Boot 项目中基本不再使用，但理解它的存在有助于看懂旧项目。

### Bean 的作用域（Scope）

作用域决定了同一个 Bean 定义创建几个实例：

| 作用域 | 说明 | 适用场景 |
|--------|------|----------|
| **singleton**（默认） | 整个容器中只有一个实例 | 无状态 Service、Controller、Repository |
| **prototype** | 每次获取都创建新实例 | 有状态的组件、每次请求需要新实例 |
| **request** | 每个 HTTP 请求一个实例 | Web 应用中请求级数据 |
| **session** | 每个 HTTP Session 一个实例 | 用户会话数据 |

默认是 singleton 的原因：绝大多数 Spring 组件是无状态的，一个实例就可以服务于所有请求，省内存、不需要反复创建对象。

### Bean 的生命周期

Spring 中的 Bean 有完整的生命周期，容器在几个关键节点提供了扩展点：

```
实例化 → 属性填充（DI）→ 初始化 → 使用 → 销毁
                           ↑
                    @PostConstruct
```

关键生命周期回调：

```java
@Component
public class DataInitializer {

    @PostConstruct
    public void init() {
        // Bean 创建完毕、依赖注入完成后执行
        // 适合做数据初始化、资源预热
    }

    @PreDestroy
    public void destroy() {
        // Bean 销毁前执行
        // 适合释放资源、关闭连接
    }
}
```

理解生命周期对排查问题很重要。比如字段注入和 Setter 注入发生在"属性填充"阶段，所以构造器里不能使用这些尚未注入的字段；构造器注入则是在调用构造器时通过参数传入，适合表达必需依赖。

### Bean 的命名

每个 Bean 在容器中有一个唯一 ID。默认情况下：

- 用 `@Component` 标记：Bean 名 = 类名首字母小写（`UserService` → `userService`）
- 用 `@Bean` 标记：Bean 名 = 方法名
- 可以用 `@Component("customName")` 或 `@Bean("customName")` 自定义

当同一个类型有多个 Bean 时（比如多数据源），就需要用 `@Qualifier("beanName")` 指定注入哪一个。

---

## 核心要点

1. **Bean = 被 Spring IoC 容器管理的普通对象。** 本质上没有特殊之处，只是生命周期由容器接管。
2. **默认作用域是 singleton。** 一个 Bean 定义对应一个实例，服务于所有请求。
3. **创建 Bean 的首选方式是通过 `@Component` 及其派生注解。** `@Bean` 用于第三方类。
4. **Bean 有完整的生命周期。** 容器提供了 `@PostConstruct` 和 `@PreDestroy` 两个关键扩展点。
5. **理解 Bean 是理解 Spring 一切上层功能的基础。** AOP 代理、事务切面、安全过滤器，最终都作用在 Bean 上。

---

## 常见误区

- **以为加了 `@Component` 就自动成为 Bean。** 前提是 Spring 的组件扫描范围包含了这个类所在的包。默认扫描 `@SpringBootApplication` 所在包及子包。如果类在包外，不会自动注册，启动时不会报错，只是该类不被管理。
- **混用 `@Component` 和 `new`。** 如果你通过 `new UserService()` 创建对象，这就是一个普通对象，不是 Bean。Spring 不会对它做任何管理 -- 不会注入依赖、不会应用 AOP、不会管理生命周期。
- **忽略 singleton 的线程安全问题。** singleton Bean 是无状态的才能线程安全。如果你在 singleton Bean 中存放可变状态（比如一个可变的 HashMap），多线程并发读写会导致问题。
- **把 prototype Bean 直接注入到 singleton Bean 中。** 注入只会发生一次（创建 singleton Bean 的时候），之后拿到的都是同一个 prototype 实例，等于在这个 singleton 内部固定住了一个实例。更常见的做法是注入 `ObjectProvider<T>` 或使用查找方法，让每次使用时再向容器获取新实例。

---

## 与其他概念的关联

- **前置：** [Java Spring IoC](./05_Java%20Spring%20IoC.md) -- IoC 是理念，Bean 是实体。理解了"为什么要把控制权交给容器"之后，Bean 就是"容器管理的到底是什么"。
- **并行：** [Java Spring 容器](./09_Java%20Spring%20容器.md) -- 容器是 Bean 的管理者，负责 Bean 的创建、配置、生命周期管理。两者是管家 vs 住户的关系。
- **后续：** [Java Spring DI](./07_Java%20Spring%20DI.md) -- Bean 创建之后，需要把依赖注入进去。DI 的"依赖"和"被依赖方"都是 Bean。[Java Spring AOP](./08_Java%20Spring%20AOP.md) -- AOP 作用在 Bean 上，通过代理 Bean 实现横切逻辑。
