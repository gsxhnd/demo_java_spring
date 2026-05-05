---
title: Java Spring 容器
created: 2026-05-04 22:40:00
category: Java-Spring
tags:
  - Java
  - Spring
  - ApplicationContext
  - BeanFactory
  - 容器
---

<!-- markdownlint-disable MD025 -->

# Java Spring 容器（ApplicationContext）

## 为什么要学容器

前面四篇讲了 IoC、Bean、DI、AOP -- 这些都是概念和机制。那谁来"执行"这一切？答案是容器（Container）。

容器是 Spring 的心脏。它读取配置、扫描类、创建 Bean、注入依赖、管理生命周期。理解容器的工作原理，你才能理解：

- 为什么加了 `@Component` 魔术就发生了
- 为什么启动慢（容器在做大量初始化工作）
- 为什么某个 Bean 找不到（容器扫描范围不对）
- 为什么 AOP 能切中你的方法（容器返回的是代理对象）

---

## 核心概念

### 容器是什么

**Spring 容器（IoC Container）是 Spring 框架的核心组件，负责实例化、配置、装配、管理 Bean 的整个生命周期。**

可以把容器理解为一张"Bean 注册表 + 工厂 + 生命周期管理器"的集合体。你提交"元数据"（注解、配置），容器根据这些元数据生产和管理 Bean。

Spring 中容器有两个核心接口：`BeanFactory`（底层基础设施，提供最基本的 DI 能力）和 `ApplicationContext`（`BeanFactory` 的扩展，增加了国际化、事件发布、AOP、Web 支持等企业级特性）。实际开发中 100% 使用 `ApplicationContext`。

### 为什么需要容器

**容器把 Spring 的各个概念（IoC、Bean、DI、AOP）从理论变成了可运行的实体。**

没有容器的话，你需要在代码中手动实现：扫描目录找注解 → 反射创建对象 → 构建依赖图 → 按顺序实例化 → 注入依赖 → 创建代理 → 调用生命周期回调。这是一个完整且复杂的流程。容器把这些都封装好了，你只需要写配置和业务代码。

### 没有容器会怎样

你需要手动实现一整套对象管理基础设施 -- 组件扫描、依赖解析、实例化排序、代理创建、生命周期回调。这本身就是一个框架级别的工程量。有了容器，你只需要写注解和业务代码，容器在启动时自动完成所有组装工作，运行时几乎没有额外开销。

---

## 概念深入解释

### 容器的核心职责

**读取配置元数据** -- 元数据告诉容器"要管理哪些类、它们之间什么关系"。来源可以是注解（`@Component`、`@Service` 等，现代标准）、Java Config（`@Configuration` + `@Bean`，用于第三方类）、XML（旧式方式，Spring Boot 项目中极少使用）。

**实例化 Bean** -- 容器根据元数据，通过反射创建 Bean 实例。这个阶段的核心工作是解析依赖图，确定实例化顺序 -- 被依赖的先创建，依赖方后创建。

**依赖注入** -- Bean 创建完成后，容器检查它的依赖需求（构造器参数、`@Autowired` 字段），从容器中找到对应的依赖 Bean 并注入。

**生命周期管理** -- 容器在 Bean 的各个生命周期节点提供回调：`@PostConstruct`（初始化后）、`@PreDestroy`（销毁前）。

**代理创建** -- 对有 AOP 切面的 Bean，容器不直接返回原始实例，而是创建一个代理对象并注册到容器中。你注入这个 Bean 时拿到的是代理对象。

### ApplicationContext 的启动流程

Spring Boot 应用的启动大致经历以下步骤：

```
1. 创建 ApplicationContext 实例
2. 注册配置类（@Configuration 标注的类）
3. 处理 BeanFactoryPostProcessor（可以在 Bean 创建前修改定义元数据）
4. 组件扫描 — 找到所有 @Component、@Service 等注解的类
5. 根据依赖图确定实例化顺序
6. 实例化单例 Bean — 按顺序通过反射调用构造器
7. 依赖注入 — 填充 @Autowired 字段、调用 setter
8. 执行 @PostConstruct 方法
9. 执行 BeanPostProcessor#postProcessAfterInitialization（AOP 代理在这里创建）
10. 内嵌服务器启动（如果是 Web 应用）
11. 容器就绪
```

这个流程解释了为什么 Spring Boot 应用"启动慢" -- 它在启动时把几乎所有的 Bean 初始化和依赖注入都完成了。但也正因为如此，运行时的响应是极快的，不需要再创建对象。

### BeanFactory vs ApplicationContext

| 特性 | BeanFactory | ApplicationContext |
|------|-------------|-------------------|
| Bean 实例化时机 | 默认按需创建 | 默认预实例化非懒加载 singleton |
| 注解后处理 | 需手动注册 | 自动注册 |
| AOP 支持 | 需手动配置 | 自动集成 |
| 事件发布 | 不支持 | 支持 |
| 国际化 | 不支持 | 支持 |

开发中永远用 `ApplicationContext`。`BeanFactory` 只在内存敏感的环境或需要极致启动速度时有意义。

### 如何获取容器中的 Bean

正常情况下你不需要手动获取 Bean -- 通过构造器注入让 Spring 自动把依赖给你。但某些特殊场景需要手动获取：

```java
@Autowired
private ApplicationContext context;

public void someMethod() {
    UserService service = context.getBean(UserService.class);
}
```

常见的手动获取场景：需要动态选择 Bean 实现时、prototype 作用域的 Bean 需要每次都重新获取、在非 Spring 管理的类中获取 Bean（遗留代码集成）。

但原则上，能通过 DI 解决的优先 DI。手动 `getBean()` 让代码跟容器耦合。

---

## 核心要点

1. **容器 = IoC 哲学的执行者、整个 Spring 框架的运转中枢。**
2. **ApplicationContext 是容器的标准实现**，提供了 Bean 创建、DI、AOP、事件等完整能力。
3. **容器在启动时完成几乎所有工作** -- 组件扫描 → 实例化 → 注入 → 代理创建 → 生命周期回调。运行时几乎没有额外开销。
4. **Spring 给你的对象可能是代理对象** -- 如果该类有 AOP 切面或事务注解，实际类型不是你的原始类。
5. **能用 DI 就不要手动 `getBean()`** -- 手动获取让代码跟容器耦合，放弃了很多自动化的好处。

---

## 常见误区

- **拿到 Bean 后直接转型为具体类。** 如果这个 Bean 有 AOP 切面，它是通过 CGLIB 代理创建的，继承自原始类。转型没问题，但要注意它其实是代理类的实例。
- **在 Bean 的构造器里调用 `ApplicationContext.getBean()`。** 此时容器还没完全就绪，某些 Bean 可能还没创建好，拿到的可能是不完整或未初始化的对象。
- **一个 `ApplicationContext` 就是一个 Spring 应用的全部。** 在单体应用中基本如此，但在 Spring Boot + Spring Cloud 微服务架构中，每个服务有自己的 ApplicationContext，还可能有父子容器关系。
- **用 `@Autowired` 注入 `ApplicationContext` 是种坏味道。** 偶尔为之没问题，但如果在代码中大量手动获取 Bean，通常意味着设计可以优化 -- 考虑用工厂模式、策略模式或 `@Qualifier` 来让容器自动选择。
- **假设所有资源都会被容器自动关闭。** 容器关闭时会调用 singleton Bean 的销毁回调，包括 `@PreDestroy`、`DisposableBean`、显式 `destroyMethod`，以及常见 `close()` / `shutdown()` 方法的推断销毁。但前提是资源对象本身是容器管理的 Bean；你在业务代码里手动 `new` 出来的连接、线程池或客户端仍然需要自己关闭。

---

## 与其他概念的关联

- **前置：** [Java Spring IoC](./05_Java%20Spring%20IoC.md) -- 容器是 IoC 理念的物理实体。[Java Spring Bean](./06_Java%20Spring%20Bean.md) -- 容器管理的对象就是 Bean。[Java Spring DI](./07_Java%20Spring%20DI.md) -- 容器执行 DI 的具体流程。[Java Spring AOP](./08_Java%20Spring%20AOP.md) -- 容器负责创建 AOP 代理对象，并在 Bean 初始化阶段织入切面。
- **并行：** 无。容器是 Part 2 的收束概念，统筹了前面所有概念。
- **后续：** [Java Spring Boot 自动配置](Java%20Spring%20Boot%20自动配置.md) -- Spring Boot 的核心增强之一就是根据 classpath 自动注册 Bean 到容器中，减少手动配置。理解容器后，自动配置就是"容器根据条件自动创建 Bean"。
