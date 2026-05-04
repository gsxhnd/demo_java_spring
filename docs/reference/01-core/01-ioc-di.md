# Spring IoC 与依赖注入 / IoC & Dependency Injection

> Spring 的核心就是 IoC 容器。理解 Bean 的创建、管理和注入机制，是掌握整个 Spring 生态的第一步。

## 1. 概述 / Overview

IoC（Inversion of Control，控制反转）是 Spring 的核心设计思想。传统编程中对象自己创建依赖，而 IoC 将对象的创建和依赖关系的管理交给容器（Container），开发者只需声明"我需要什么"，容器负责"怎么给你"。

DI（Dependency Injection，依赖注入）是 IoC 的具体实现方式，通过构造器、Setter 或字段注入将依赖传递给目标对象。

## 2. 术语表 / Glossary

> 这些是 Spring IoC 容器中最核心的术语。理解它们的定义、作用和设计目的，是阅读后续内容的基础。

| 术语 | 定义 | 作用 | 为什么存在 |
|------|------|------|-----------|
| **IoC（控制反转）** | 将"对象创建和依赖管理"的控制权从代码内部移交给外部容器。不是你自己 `new`，而是告诉容器你要什么，容器给你。 | 解耦——调用方不再需要知道依赖的具体实现类和创建方式。 | 传统方式中对象自己管理依赖，导致代码高耦合、难测试。IoC 把这份职责外包给容器，让对象只关心自己的业务逻辑。 |
| **DI（依赖注入）** | IoC 的具体实现方式：容器在运行时将被依赖的对象"注入"到目标对象中（通过构造器、Setter 或字段）。 | 自动装配——声明依赖关系后，容器自动完成注入，无需手动调用 `getBean()`。 | IoC 是一种思想，DI 是把这个思想落地的技术手段。没有 DI，IoC 就只是概念而无法实践。 |
| **Spring Bean** | 由 Spring IoC 容器创建、装配、管理的 Java 对象实例。Bean 的生命周期（创建→初始化→使用→销毁）完全由容器控制。 | 承载应用的所有功能组件——Service、Repository、Controller、配置对象等，都是 Bean。 | 将"对象创建"和"对象使用"彻底分离。开发者声明"我需要 UserRepository"，容器负责在正确时机创建它、注入需要的依赖、调用初始化回调，并在应用关闭时执行销毁逻辑。不是 Spring 管理的对象（`new` 出来的）叫 POJO，不享受容器的任何能力。 |
| **容器 (ApplicationContext)** | Spring IoC 容器的核心接口，负责 Bean 的注册、查找、生命周期管理。继承了 `BeanFactory`、`MessageSource`、`EventPublisher` 等多个能力。 | 运行时的"大管家"——启动时扫描并注册所有 Bean，运行时按需提供 Bean 实例。 | 将零散的 Bean 组织成统一管理的整体。`ApplicationContext` 不仅是 Bean 工厂，还提供国际化、事件发布、资源加载等企业级能力。 |
| **`@Component` 系列注解** | 标记一个类为 Spring Bean 的注解。`@Service`、`@Repository`、`@Controller` 都继承自 `@Component`，语义更明确。 | 让 `@ComponentScan` 自动发现并注册 Bean，无需手动 `@Bean` 声明。 | 约定优于配置——你自己写的类用 `@Component` 自动注册，第三方类用 `@Bean` 手动注册。分工清晰。 |
| **`@Configuration` + `@Bean`** | Java 代码方式注册 Bean：`@Configuration` 标记配置类，`@Bean` 标记方法（返回值自动注册到容器）。 | 注册第三方库的类、需要复杂初始化逻辑的对象、或同一类型的多个不同实例。 | 不是所有类都能加 `@Component`（比如 JDK 自带的 `String`）。`@Bean` 给了开发者完全的实例化控制权。 |
| **Scope（作用域）** | 定义 Bean 的生存范围：`singleton`（默认，全局单例）或 `prototype`（每次获取新建）。 | 控制 Bean 实例的创建次数和生命周期。 | 不同场景需要不同的实例策略。比如状态化的 Bean 应该用 prototype（每次不同），无状态的 Service 应该用 singleton（节省内存）。 |
| **`@PostConstruct` / `@PreDestroy`** | Bean 生命周期回调：`@PostConstruct` 在依赖注入完成后调用，`@PreDestroy` 在容器销毁前调用。 | 在 Bean 的就绪和销毁时点执行自定义逻辑（如初始化资源、关闭连接池）。 | Bean 的初始化顺序由容器控制，开发者无法预测。用回调替代手动调用，确保在正确的时机执行。 |
| **`@Primary` / `@Qualifier`** | 当容器中有多个同类型的 Bean 时，`@Primary` 指定默认选择，`@Qualifier` 按名称精确指定。 | 解决"同类型多 Bean"的注入歧义。 | 实际项目中经常需要同一接口的多个实现（如 `DevEmailService` / `ProdEmailService`），需要有机制指定注入哪个。 |
| **BeanPostProcessor** | 一个钩子接口，在**每个 Bean 初始化前后**插入自定义逻辑。Spring AOP、事务代理、`@Async` 等底层都基于此实现。 | 在不修改 Bean 代码的前提下，统一对所有 Bean 执行额外处理（如代理增强）。 | 横切关注点的实现机制——如果要对 100 个 Service 都加日志，不需要修改 100 个类，写一个 `BeanPostProcessor` 即可。 |

## 3. 核心概念 / Core Concepts

### 2.1 IoC 容器架构

```
                    ┌─────────────────────────────────────┐
                    │         BeanFactory (根接口)          │
                    │   - getBean() / containsBean()      │
                    └──────────────┬──────────────────────┘
                                   │ 继承
                    ┌──────────────▼──────────────────────┐
                    │      ApplicationContext (常用)        │
                    │   - 国际化 (MessageSource)            │
                    │   - 事件发布 (ApplicationEventPublisher)│
                    │   - 资源加载 (ResourceLoader)          │
                    │   - 环境抽象 (Environment)             │
                    └──────────────┬──────────────────────┘
                                   │ 实现
                 ┌─────────────────┼─────────────────┐
                 ▼                 ▼                 ▼
    AnnotationConfigAC    ClassPathXmlAC    WebApplicationContext
    (注解驱动，主流)       (XML，遗留项目)     (Web 环境)
```

### 2.2 Bean 注册方式对比

| 方式 | 注解/配置 | 适用场景 | 说明 |
|------|----------|---------|------|
| 组件扫描 | `@Component` `@Service` `@Repository` `@Controller` | 自己写的类 | 配合 `@ComponentScan` 自动发现 |
| Java Config | `@Bean` in `@Configuration` | 第三方库的类 | 手动实例化并注册 |
| 条件注册 | `@Conditional` 系列 | 按条件决定是否注册 | Spring Boot 自动配置的基础 |
| Import | `@Import` `@ImportResource` | 导入其他配置 | 模块化配置组织 |

### 2.3 依赖注入方式对比

| 方式 | 写法 | 推荐度 | 说明 |
|------|------|--------|------|
| 构造器注入 | 构造函数参数 | ★★★★★ | Spring 官方推荐，不可变、易测试、防循环依赖 |
| Setter 注入 | `@Autowired` on setter | ★★★☆☆ | 可选依赖场景 |
| 字段注入 | `@Autowired` on field | ★★☆☆☆ | 简洁但难测试，不推荐生产使用 |

### 2.4 Bean 生命周期

```
实例化 (Instantiation)
    │
    ▼
属性填充 (Populate Properties / DI)
    │
    ▼
Aware 接口回调 (BeanNameAware, ApplicationContextAware, ...)
    │
    ▼
BeanPostProcessor#postProcessBeforeInitialization
    │
    ▼
初始化 (@PostConstruct / InitializingBean#afterPropertiesSet / init-method)
    │
    ▼
BeanPostProcessor#postProcessAfterInitialization  ← AOP 代理在此创建
    │
    ▼
Bean 就绪，放入容器
    │
    ▼
销毁 (@PreDestroy / DisposableBean#destroy / destroy-method)
```

### 2.5 Bean 作用域 / Scope

| Scope | 说明 | 生命周期 |
|-------|------|---------|
| `singleton` | 默认，整个容器一个实例 | 容器启动 → 容器关闭 |
| `prototype` | 每次 getBean 创建新实例 | 容器不管理销毁 |
| `request` | 每个 HTTP 请求一个实例 | 请求开始 → 请求结束 |
| `session` | 每个 HTTP Session 一个实例 | Session 创建 → Session 过期 |
| `application` | 每个 ServletContext 一个实例 | 应用启动 → 应用关闭 |

### 2.6 条件装配 / Conditional

| 注解 | 条件 | 典型用途 |
|------|------|---------|
| `@ConditionalOnClass` | classpath 存在指定类 | 自动配置：有驱动才配数据源 |
| `@ConditionalOnMissingBean` | 容器中不存在指定 Bean | 用户自定义优先 |
| `@ConditionalOnProperty` | 配置属性满足条件 | 功能开关 |
| `@ConditionalOnWebApplication` | 是 Web 应用 | Web 相关自动配置 |
| `@Profile` | 激活指定 Profile | 多环境差异化配置 |

## 4. 快速集成 / Quick Start

### 3.1 Maven 依赖

| 场景 | 依赖 | 说明 |
|------|------|------|
| Spring Boot Web | `spring-boot-starter-web` | 包含 Spring Core + MVC + 内嵌 Tomcat |
| 纯 Spring Core | `spring-boot-starter` | 不含 Web，适合 CLI 或后台任务 |

### 3.2 关键配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.main.allow-bean-definition-overriding` | `false` | 是否允许 Bean 定义覆盖 |
| `spring.main.lazy-initialization` | `false` | 全局懒加载（加速启动，延迟发现问题） |
| `spring.main.allow-circular-references` | `false` | 是否允许循环依赖（Boot 2.6+ 默认禁止） |

## 5. 设计决策与实现原理 / Design Decisions

> 以下结合 [`examples/spring-ioc-demo/`](../../examples/spring-ioc-demo/) 的实际代码，解释每个设计选择背后的"为什么"。

### 4.1 为什么用 `ObjectProvider<EmailService>` + `@Lazy` 而不是 field `@Autowired @Lazy`？

```java
// ✅ 演示中的做法：构造器注入 + ObjectProvider + @Lazy
public UserService(UserRepository userRepository,
                   @Lazy ObjectProvider<EmailService> emailServiceProvider) { ... }
```

- **`ObjectProvider` 是函数式的延迟获取**：调用 `getIfAvailable()` 时才真正从容器获取 Bean，语义明确
- **Field `@Lazy`** 只延迟注入代理对象，但字段上的 `@Autowired` 容易在单元测试中被忽略（需要反射注入）
- **构造器注入更可测试**：测试时可以手动传入一个 Mock 的 `ObjectProvider`，无需启动 Spring 容器
- **显式 > 隐式**：`ObjectProvider` 明确表达了"这个依赖可能不会被用到"的意图

### 4.2 为什么 `@ComponentScan(basePackages = "com.example.ioc")` 显式指定？

默认 `@SpringBootApplication` 只扫描启动类所在包及子包。显式指定 `basePackages` 是教学目的——让学习者一眼看清扫描范围，避免因包结构不对导致的 Bean 找不到问题。

### 4.3 为什么 prototype scope 用 `System.currentTimeMillis()` 作为 ID？

```java
@Bean @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public User prototypeUser() {
    return new User(System.currentTimeMillis(), "Prototype User", ...);
}
```

每次 `getBean()` 调用都会执行 `@Bean` 方法，生成不同的时间戳 ID。通过 `/api/ioc/scope-demo` 接口对比两次获取的 Bean 引用相等性 (`==`)，直观证明 prototype 每次都创建新实例。

### 4.4 为什么 `DevEmailService` 同时标注 `@Profile({"dev", "default"})`？

`"default"` 是 Spring 的特殊 Profile：当没有任何 Profile 被显式激活时自动生效。加上 `"dev"` 后，无论是 `spring.profiles.active=dev` 还是未设置任何 Profile，都能注册该 Bean——保证开箱即用。

### 4.5 为什么 `CustomBeanPostProcessor` 用 `@Component` 注册？

`BeanPostProcessor` 本身也是一个 Spring Bean。用 `@Component` 让容器自动发现并注册它，Spring 会将其应用于**所有**其他 Bean 的初始化过程。这展示了 Spring AOP 的底层基础——`BeanPostProcessor` 不需要特殊注册方式，只是普通的 Bean。

### 4.6 为什么 IoC Demo 选择 Web 模式（`@RestController`）而非纯 CLI？

命令行 `ApplicationRunner` 也能打印生命周期日志，但 Web API 提供了**交互式验证**能力：
- `/api/ioc/scope-demo` → 对比 singleton vs prototype 的引用相等性
- `/api/ioc/lazy-demo` → 触发延迟注入，观察 `EmailService` 何时被创建
- `/api/ioc/message` → 验证 `@Primary` 选择行为

这些行为通过浏览器或 curl 直接触发，比看启动日志更直观。

### 4.7 为什么 `application.yml` 开启 `allow-bean-definition-overriding: true`？

Demo 中 `AppConfig` 注册了两个同类型的 `MessageService` Bean（一个标注 `@Primary`，另一个不标注）。开启 Bean 覆盖允许同一个 Bean 名称被后续定义覆盖，配合 `@Primary` 展示类型冲突解决机制。**生产环境应保持默认值 `false`，避免意外覆盖。**

### 4.8 为什么用 `ConcurrentHashMap` + `AtomicLong` 做内存存储？

- **线程安全**：`ConcurrentHashMap` 保证多线程并发访问安全
- **零外部依赖**：无需安装数据库，clone 即可运行
- **代码精简**：Demo 的重点是 IoC 概念，不应被数据库配置分散注意力

### 4.9 为什么全项目坚持构造器注入（`private final` + constructor）？

```java
// ✅ 推荐：构造器注入
private final UserRepository userRepository;
public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
```

| 原因 | 说明 |
|------|------|
| **不可变性** | `final` 字段保证注入后不会被篡改 |
| **易测试** | 测试时 `new UserService(mockRepo)` 即可，无需启动 Spring 容器 |
| **编译期安全** | 构造器强制传入依赖，不会出现 NPE |
| **防循环依赖** | 构造器注入的循环依赖在容器启动时就报错（fail-fast），而非运行时 |
| **Spring 官方推荐** | 从 Spring Framework 4.3 起，单构造器场景无需 `@Autowired` |

## 6. 进阶要点 / Advanced Topics

> 以下进阶要点为概念性说明，具体代码演示见上方 [设计决策与实现原理](#4-设计决策与实现原理--design-decisions) 中引用的示例项目。

- **BeanPostProcessor** — 在 Bean 初始化前后插入自定义逻辑，AOP、事务代理都基于此机制
- **BeanFactoryPostProcessor** — 在 Bean 实例化之前修改 BeanDefinition，如 PropertySourcesPlaceholderConfigurer 处理 `${}` 占位符
- **FactoryBean** — 自定义复杂 Bean 的创建逻辑，MyBatis 的 `SqlSessionFactoryBean` 就是典型案例
- **循环依赖与三级缓存** — Spring 通过 singletonObjects / earlySingletonObjects / singletonFactories 三级缓存解决 Setter 注入的循环依赖，但构造器注入无法解决（这也是推荐构造器注入的原因之一）
- **`@Lazy` 延迟注入** — 打破循环依赖的另一种方式，注入代理对象，首次使用时才真正初始化
- **`@Lookup` 方法注入** — 在 singleton Bean 中获取 prototype Bean 的正确方式
- **`ObjectProvider<T>`** — 安全的延迟/可选依赖获取，替代 `@Autowired(required=false)`
- **构造器绑定** — `@ConfigurationProperties` 支持 Record 类型，不可变配置

## 7. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| `NoSuchBeanDefinitionException` | Bean 未注册到容器 | 检查 `@Component` 注解和 `@ComponentScan` 扫描路径 |
| `NoUniqueBeanDefinitionException` | 同类型多个 Bean | 使用 `@Primary` 或 `@Qualifier` 指定 |
| 循环依赖报错 | 构造器注入形成环 | 重构设计，或用 `@Lazy` 打破环 |
| `@Autowired` 注入为 null | 对象不是 Spring 管理的 | 确保通过容器获取 Bean，而非 `new` |
| Bean 覆盖报错 | Boot 2.1+ 默认禁止覆盖 | 设置 `allow-bean-definition-overriding=true` 或重命名 |
| `@PostConstruct` 中依赖未就绪 | 依赖的 Bean 还在初始化 | 改用 `ApplicationRunner` 或 `@EventListener(ApplicationReadyEvent)` |

## 8. 示例项目 / Example

> 示例项目位于 [`examples/spring-ioc-demo/`](../../examples/spring-ioc-demo/)
>
> 已演示：Bean 注册方式（`@Component` / `@Bean` / `@Configuration`）、构造器注入、`ObjectProvider` 延迟注入、`@Primary` 类型冲突解决、singleton vs prototype 作用域、`@Profile` 环境切换、`@PostConstruct` / `@PreDestroy` 生命周期回调、`@ConditionalOn*` 条件装配、`BeanPostProcessor` 拦截

## 9. 参考链接 / References

- [Spring Framework Reference — IoC Container](https://docs.spring.io/spring-framework/reference/core/beans.html)
- [Spring Framework Reference — Bean Scopes](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html)
- [Baeldung — Spring Dependency Injection](https://www.baeldung.com/spring-dependency-injection)
- [Baeldung — Circular Dependencies in Spring](https://www.baeldung.com/circular-dependencies-in-spring)

## 10. 下一步

理解了 IoC 容器如何管理 Bean 之后，下一步学习 Spring MVC — 了解 Spring 如何基于 IoC 容器构建 Web 层，将 Bean 暴露为 RESTful API。

→ [Spring MVC Web 开发](02-spring-mvc.md)