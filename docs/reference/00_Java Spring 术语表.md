---
title: Java Spring 术语表
created: 2026-05-05 00:16:00
tags:
  - Java
  - Spring
  - 术语表
  - 参考
---

<!-- markdownlint-disable MD025 -->

# Java Spring 术语表

遇到不确定的概念时，来这里查一下。

---

## Java 语言基础

### Annotation（注解）

一种元数据机制。注解本身不改变代码逻辑，但可以被编译器或框架读取，从而影响程序行为。Spring 的配置几乎全部通过注解完成。

→ [Java 注解机制](./02_Java%20注解机制.md)

### 元注解（Meta-Annotation）

"注解的注解"。用来定义注解本身的行为。最常用的两个：

- **@Retention** -- 注解的生命周期（`SOURCE`、`CLASS`、`RUNTIME`）
- **@Target** -- 注解可以贴在哪里（`TYPE`、`METHOD`、`FIELD`、`PARAMETER` 等）

→ [Java 注解机制](./02_Java%20注解机制.md#注解是什么)

### 组合注解（Composed Annotation）

一个注解内部包含其他注解。Spring 大量使用这种模式简化配置。典型例子：`@SpringBootApplication` = `@SpringBootConfiguration` + `@EnableAutoConfiguration` + `@ComponentScan`。

→ [Java 注解机制](./02_Java%20注解机制.md)

### Reflection（反射）

Java 提供的一种机制，允许程序在运行时检查和操作类的结构（字段、方法、构造器、注解等）。Spring 用反射扫描类、读注解、创建实例、注入依赖。

→ [Java 反射基础](./03_Java%20反射基础.md)

### Class 对象

反射的入口。每个 Java 类在 JVM 中都有一个对应的 `Class` 对象，通过它可以获取类的所有结构信息。获取方式：`Class.forName("...")`、`Xxx.class`、`obj.getClass()`。

→ [Java 反射基础](./03_Java%20反射基础.md)

### Lambda 表达式

匿名函数的简写语法，Java 8 引入。本质是函数式接口的实例。语法：`(参数) -> 表达式`。Spring 5+ 大量使用 Lambda 做配置 DSL。

→ [Java Lambda 与函数式接口](./04_Java%20Lambda%20与函数式接口.md)

### Functional Interface（函数式接口）

只有一个抽象方法的接口。Lambda 表达式的类型载体。用 `@FunctionalInterface` 标记（可选）。四大内置：`Function<T,R>`（转换）、`Consumer<T>`（消费）、`Supplier<T>`（供给）、`Predicate<T>`（判断）。

→ [Java Lambda 与函数式接口](./04_Java%20Lambda%20与函数式接口.md)

### Method Reference（方法引用）

Lambda 的进一步简写。当 Lambda 体只调用一个已有方法时，用 `::` 语法替代。四种形式：静态方法引用、实例方法引用（特定对象）、实例方法引用（任意对象）、构造器引用。

→ [Java Lambda 与函数式接口](./04_Java%20Lambda%20与函数式接口.md)

### POJO（Plain Old Java Object）

普通的 Java 对象，不继承特定框架的类、不实现特定框架的接口。Spring 推崇 POJO 编程 -- 你的业务类应该是纯粹的 Java 类，框架通过注解和代理来增强它们，而不是要求你继承框架基类。

---

## Spring 核心概念

### IoC（Inversion of Control，控制反转）

一种设计原则：将对象创建和依赖管理的控制权从程序代码反转给外部容器。你只声明"我需要什么"，容器负责"怎么给你"。IoC 是 Spring 的设计哲学基石。

→ [Java Spring IoC](./05_Java%20Spring%20IoC.md)

### DI（Dependency Injection，依赖注入）

IoC 最主流的具体实现方式。容器在创建 Bean 时，自动把它需要的依赖"注入"进去。三种方式：构造器注入（推荐）、Setter 注入、字段注入（不推荐）。

→ [Java Spring DI](./07_Java%20Spring%20DI.md)

### Bean

由 Spring IoC 容器管理的对象。任何普通 Java 类的实例，一旦交给容器管理，就是一个 Bean。Bean 有完整的生命周期（创建 → 初始化 → 使用 → 销毁），由容器控制。

→ [Java Spring Bean](./06_Java%20Spring%20Bean.md)

### Bean 作用域（Scope）

决定同一个 Bean 定义创建几个实例。**singleton**（默认，整个容器一个实例）、**prototype**（每次获取创建新实例）、**request**（每个 HTTP 请求一个）、**session**（每个 Session 一个）。

→ [Java Spring Bean](./06_Java%20Spring%20Bean.md)

### Bean 生命周期

Bean 从创建到销毁的完整过程：实例化 → 属性填充（DI）→ 初始化（`@PostConstruct`）→ 使用 → 销毁（`@PreDestroy`）。容器在每个阶段提供扩展点。

→ [Java Spring Bean](./06_Java%20Spring%20Bean.md)

### AOP（Aspect-Oriented Programming，面向切面编程）

一种编程范式，把横切关注点（日志、事务、安全等）从业务逻辑中分离出来，以"切面"的形式统一管理。Spring AOP 底层通过动态代理实现。

→ [Java Spring AOP](./08_Java%20Spring%20AOP.md)

### Aspect（切面）

横切关注点的模块化。一个 `@Aspect` 标注的类，包含 Pointcut 和 Advice 的定义。

→ [Java Spring AOP](./08_Java%20Spring%20AOP.md)

### Pointcut（切入点）

定义切面在哪些方法上生效的匹配表达式。常用类型：`execution`（匹配方法签名）、`@annotation`（匹配注解）、`within`（匹配类/包）。

→ [Java Spring AOP](./08_Java%20Spring%20AOP.md)

### Advice（通知）

在切入点上执行的具体逻辑。五种类型：`@Before`（方法前）、`@AfterReturning`（正常返回后）、`@AfterThrowing`（异常后）、`@After`（结束后）、`@Around`（环绕，最强大）。

→ [Java Spring AOP](./08_Java%20Spring%20AOP.md)

### Join Point（连接点）

程序执行过程中的某个时刻。在 Spring AOP 中，连接点几乎总是方法调用。

→ [Java Spring AOP](./08_Java%20Spring%20AOP.md)

### Weaving（织入）

把切面应用到目标对象上的过程。Spring AOP 在运行时通过动态代理织入。

→ [Java Spring AOP](./08_Java%20Spring%20AOP.md)

### 横切关注点（Cross-Cutting Concerns）

散落在多个模块中、与核心业务逻辑无关但又必须存在的功能。典型例子：日志、事务、安全、缓存、监控。AOP 就是为了解决横切关注点的代码重复问题。

→ [Java Spring AOP](./08_Java%20Spring%20AOP.md)

---

## Spring 容器与配置

### ApplicationContext（应用上下文）

Spring 的核心容器实现，负责创建、配置、管理所有 Bean。是 `BeanFactory` 的企业级扩展，增加了 AOP、事件发布、国际化等能力。

→ [Java Spring 容器](./09_Java%20Spring%20容器.md)

### BeanFactory

Spring 容器的底层接口，提供最基本的 DI 能力。`ApplicationContext` 继承自 `BeanFactory`。实际开发中不直接使用 `BeanFactory`。

→ [Java Spring 容器](./09_Java%20Spring%20容器.md)

### Component Scan（组件扫描）

容器在启动时扫描指定包下的所有类，找到带 `@Component` 及其派生注解的类，自动注册为 Bean。默认扫描 `@SpringBootApplication` 所在包及子包。

→ [Java Spring 容器](./09_Java%20Spring%20容器.md)

### BeanPostProcessor

Bean 后处理器。容器在 Bean 初始化前后调用的扩展点。AOP 代理就是在 `postProcessAfterInitialization` 阶段创建的。

→ [Java Spring 容器](./09_Java%20Spring%20容器.md)

### 动态代理（Dynamic Proxy）

Spring AOP 的底层实现机制。容器给有切面的 Bean 生成代理对象，方法调用先经过代理再到达目标。两种方式：

- **JDK 动态代理** -- 基于接口
- **CGLIB 代理** -- 基于继承生成子类代理，Spring Boot 在默认 AOP 配置下倾向使用这种方式

→ [Java Spring AOP](./08_Java%20Spring%20AOP.md)

---

## Spring Boot 核心概念

### Auto-Configuration（自动配置）

Spring Boot 的核心机制：根据 classpath 中存在的类和已定义的 Bean，有条件地自动注册 Bean 到 IoC 容器中。通过条件注解（`@ConditionalOnClass`、`@ConditionalOnMissingBean` 等）决定是否生效。

→ [Java Spring Boot 自动配置](./11_Java%20Spring%20Boot%20自动配置.md)

### Starter

一组预定义的依赖描述符（POM）。引入一个 Starter 就等于引入了实现某类功能所需的全部依赖，且版本已经过兼容性测试。命名规则：官方 `spring-boot-starter-*`，第三方 `*-spring-boot-starter`。

→ [Java Spring Boot Starter](./12_Java%20Spring%20Boot%20Starter.md)

### BOM（Bill of Materials）

依赖版本清单。Spring Boot 通过 `spring-boot-dependencies` BOM 统一管理几百个库的版本兼容性。声明了 Spring Boot 版本后，所有 Starter 中的依赖版本自动确定。

→ [Java Spring Boot Starter](./12_Java%20Spring%20Boot%20Starter.md)

### Profile

Spring Boot 的多环境配置机制。通过 `application-{profile}.yml` 文件和 `spring.profiles.active` 属性，实现同一份代码在不同环境（dev、test、prod）中使用不同配置。

→ [Java Spring Boot 配置](./14_Java%20Spring%20Boot%20配置.md)

### Actuator

Spring Boot 的运维端点模块。提供健康检查（`/actuator/health`）、指标暴露（`/actuator/metrics`）、环境信息（`/actuator/env`）等 HTTP 端点，用于查看应用内部状态。

→ [Java Spring 可观测性](./15_Java%20Spring%20可观测性.md)

### OpenTelemetry（OTel）

CNCF 的可观测性标准，定义了 Traces、Metrics、Logs 的采集和导出规范。Spring Boot 通过 Micrometer + OTel Exporter 或 OTel Java Agent 集成。

→ [Java Spring 可观测性](./15_Java%20Spring%20可观测性.md)

### Micrometer

Spring 的指标门面（类似 SLF4J 之于日志）。提供统一的指标 API，底层可对接 Prometheus、Datadog、InfluxDB 等监控系统。Spring Boot Actuator 的 metrics 端点基于 Micrometer 实现。

→ [Java Spring 可观测性](./15_Java%20Spring%20可观测性.md)

### 条件注解（Conditional Annotations）

Spring Boot 自动配置的核心武器。决定一个配置类或 Bean 定义是否生效。常用：`@ConditionalOnClass`（classpath 有某类）、`@ConditionalOnMissingBean`（容器中无某 Bean）、`@ConditionalOnProperty`（配置属性满足条件）。

→ [Java Spring Boot 自动配置](./11_Java%20Spring%20Boot%20自动配置.md)

---

## 架构与设计术语

### 分层架构

Spring 应用的典型分层：`Controller`（接收请求）→ `Service`（业务逻辑）→ `Repository`（数据访问）→ Database。每一层都是 Bean，通过 DI 连接。

### DTO（Data Transfer Object）

数据传输对象。专门用于 API 层数据传输，避免直接暴露数据库 Entity。

### Entity

数据库表的 Java 映射对象。用 `@Entity` 标注，字段对应表的列。

### ORM（Object-Relational Mapping）

对象关系映射。把数据库表映射为 Java 对象，用面向对象的方式操作数据库。JPA 是 Java 的 ORM 规范，Hibernate 是最流行的实现。

### Filter Chain（过滤器链）

Spring Security 的核心机制。请求到达 Controller 之前，经过一系列过滤器进行安全检查（认证、授权等）。

### Circuit Breaker（熔断器）

微服务中的保护机制。当下游服务故障时，快速失败而不是无限等待，防止故障扩散。

---

## 容易混淆的术语对比

| 对比项 | 区别 |
|--------|------|
| **IoC vs DI** | IoC 是设计原则（控制权交给容器），DI 是实现方式（容器把依赖注入进来） |
| **Bean vs 普通对象** | Bean 由容器管理（享受 DI、AOP），普通对象通过 `new` 创建（Spring 不知道它） |
| **@Component vs @Bean** | `@Component` 贴在类上（组件扫描发现），`@Bean` 写在方法上（手动定义，适合第三方类） |
| **BeanFactory vs ApplicationContext** | BeanFactory 是底层接口（按需创建），ApplicationContext 是企业级扩展（预实例化、AOP、事件） |
| **Spring vs Spring Boot** | Spring 是基础框架（需手动配置），Spring Boot 是脚手架（自动配置 + 内嵌服务器） |
| **JDK 代理 vs CGLIB 代理** | JDK 基于接口，CGLIB 基于继承（Spring Boot 默认） |

---

## 文档索引

- 学习路线总览：[Java Spring](./01_Java%20Spring.md)
- Part 1 准备阶段：[注解](./02_Java%20注解机制.md) → [反射](./03_Java%20反射基础.md) → [Lambda](./04_Java%20Lambda%20与函数式接口.md)
- Part 2 核心概念：[IoC](./05_Java%20Spring%20IoC.md) → [Bean](./06_Java%20Spring%20Bean.md) → [DI](./07_Java%20Spring%20DI.md) → [AOP](./08_Java%20Spring%20AOP.md) → [容器](./09_Java%20Spring%20容器.md)
- Part 3 Spring Boot 起步：[概述](./10_Java%20Spring%20Boot%20概述.md) → [自动配置](./11_Java%20Spring%20Boot%20自动配置.md) → [Starter](./12_Java%20Spring%20Boot%20Starter.md) → [项目结构](./13_Java%20Spring%20Boot%20项目结构.md) → [配置](./14_Java%20Spring%20Boot%20配置.md) → [可观测性](./15_Java%20Spring%20可观测性.md)
