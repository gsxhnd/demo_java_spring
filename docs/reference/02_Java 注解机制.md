---
title: Java 注解机制
created: 2026-05-04 21:00:00
category: Java-Spring
tags:
  - Java
  - Annotation
  - Spring-前置
---

<!-- markdownlint-disable MD025 -->

# Java 注解机制

## 为什么要先学注解

打开任何一个 Spring Boot 项目，你会看到满屏的 `@` 符号：`@RestController`、`@Autowired`、`@Service`、`@GetMapping`... 这些都是注解（Annotation）。Spring 几乎把所有配置都建立在注解之上，如果不理解注解是什么、怎么工作的，后面学 Spring 会一头雾水。

所以注解是学习 Spring 的第一个前置知识。

---

## 核心概念

### 注解是什么

**Annotation（注解）是一种元数据机制。** 它本身不改变代码的执行逻辑，但可以被编译器或框架在编译时、加载时、运行时读取，从而影响程序的行为。

你可以把它理解为"贴在代码上的标签"。`@Override` 告诉编译器"这个方法是重写父类的"，编译器会检查签名是否匹配。Spring 的注解则是运行时行为 — 容器启动时扫描所有类，读取注解信息，据此决定哪些类要实例化、怎么注入依赖。

### 为什么需要注解

Spring 几乎把所有配置都建立在注解之上 -- 声明组件（`@Component`）、注入依赖（`@Autowired`）、映射路由（`@GetMapping`）。没有注解，这些配置要么写在冗长的 XML 里，要么硬编码在 Java 代码中。注解让配置变得声明式、就近化、可读性强。

### 没有注解会怎样

在 Spring 早期以及注解配置流行之前，Spring 的配置主要写在 XML 文件中。每个 Bean 的类名、依赖关系、初始化方法都要在 XML 里手动声明，与代码完全分离。改了类名要同步改 XML，漏改就会在运行时报错。注解把配置"搬回"到代码旁边，让配置和实现更容易保持一致，也让 IDE 能做更多重构支持。

---

## 概念深入解释

### 注解的三个层次

理解注解需要区分三件事：定义注解、使用注解、读取注解。

**使用注解** 是最基础的层次，也是日常开发中最常用的。直接在代码上加注解即可：

```java
@RestController
@RequestMapping("/users")
public class UserController {
    // ...
}
```

**定义注解** 用 `@interface` 关键字，配合两个关键的元注解：

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogExecutionTime {
    // 这个注解没有属性，只是一个标记
}
```

- **`@Retention`** -- 注解的生命周期。`RUNTIME` 表示运行时仍然存在，可以被反射读取。Spring 的注解几乎都是 `RUNTIME` 级别。
- **`@Target`** -- 注解可以贴在哪里。`METHOD` 表示只能贴在方法上。还有 `TYPE`（类/接口）、`FIELD`（字段）、`PARAMETER`（参数）等。

**读取注解** 依赖反射机制，在运行时检查某个元素上是否存在特定注解：

```java
Method method = obj.getClass().getMethod("someMethod");
if (method.isAnnotationPresent(LogExecutionTime.class)) {
    // 执行日志记录逻辑
}
```

Spring 框架在启动时做的事情，本质上就是大规模的注解读取和处理。

### 注解的属性

注解可以携带参数，这些参数叫做注解的属性：

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RateLimit {
    int maxRequests() default 100;
    int windowSeconds() default 60;
}
```

属性支持的类型有限：基本类型、String、Class、枚举、注解、以及这些类型的数组。不支持任意对象。

如果注解只有一个属性且名为 `value`，使用时可以省略属性名：`@RequestMapping("/users")` 等价于 `@RequestMapping(value = "/users")`。


## Spring 中的注解分类

在 Spring 生态中，注解大致可以分为几类：

**组件声明类** -- 告诉 Spring "这个类需要被管理"：

- `@Component` -- 通用组件
- `@Service` -- 业务逻辑层
- `@Repository` -- 数据访问层
- `@Controller` / `@RestController` -- Web 控制层

这四个注解在功能上几乎等价（都会被 Spring 扫描并注册为 Bean），区别在于语义 -- 它们表达了类在架构中的角色。

**依赖注入类** -- 告诉 Spring "把依赖给我"：

- `@Autowired` -- 自动注入
- `@Qualifier` -- 指定注入哪个 Bean
- `@Value` -- 注入配置值

**配置类** -- 告诉 Spring "怎么配置"：

- `@Configuration` -- 标记配置类
- `@Bean` -- 手动定义 Bean
- `@EnableAutoConfiguration` -- 启用自动配置
- `@SpringBootApplication` -- 组合注解（包含上面三个的能力）

**Web 类** -- 处理 HTTP 请求：

- `@RequestMapping` / `@GetMapping` / `@PostMapping` -- 路由映射
- `@RequestBody` / `@ResponseBody` -- 请求体/响应体处理
- `@PathVariable` / `@RequestParam` -- 参数绑定


## 组合注解（Composed Annotation）

Spring 大量使用组合注解 -- 一个注解内部包含其他注解。比如 `@SpringBootApplication` 实际上是三个注解的组合：

```java
@SpringBootConfiguration  // 包含 @Configuration
@EnableAutoConfiguration
@ComponentScan
public @interface SpringBootApplication { }
```

这意味着在启动类上加 `@SpringBootApplication`，等于同时声明了"这是配置类"、"启用自动配置"、"扫描组件"。理解组合注解有助于你在遇到问题时追溯：某个行为到底是哪个注解触发的。

---

## 核心要点

1. **注解是元数据，不是代码逻辑。** 它需要配合"读取者"（编译器、框架）才能发挥作用。
2. **Spring 的注解几乎都是 `RUNTIME` 级别**，通过反射在运行时读取。
3. **`@Retention` 决定注解活多久，`@Target` 决定注解能贴在哪。** 这两个元注解是定义注解时的必填项。
4. **Spring 的组件注解（`@Component`、`@Service` 等）功能等价，区别在于语义。**
5. **组合注解是 Spring 简化配置的重要手段。** 遇到不理解的注解，可以点进去看它组合了哪些注解。

---

## 常见误区

- **以为注解会自动生效。** 注解本身什么都不做，必须有框架或工具去读取和处理它。在纯 Java 项目中加 `@Autowired` 不会有任何效果，只有在 Spring 容器中才有意义。
- **混淆编译时注解和运行时注解。** `@Override` 是编译时检查，编译完就没了；`@Component` 是运行时注解，Spring 启动时才读取。两者机制完全不同。
- **忽略 `@Target` 的限制。** 如果一个注解的 Target 是 `METHOD`，你把它贴在类上，编译器会报错。遇到"注解不生效"的问题时，先检查 Target 是否匹配。

---

## 与其他概念的关联

- **前置：** 无。这是 Part 1 的第一个主题。
- **并行：** 无。
- **后续：** [Java 反射基础](./03_Java%20反射基础.md) -- 注解的运行时读取依赖反射机制，理解注解后自然需要理解反射。[Java Spring IoC](./05_Java%20Spring%20IoC.md)、[Java Spring Bean](./06_Java%20Spring%20Bean.md)、[Java Spring DI](./07_Java%20Spring%20DI.md) 都建立在注解之上。
