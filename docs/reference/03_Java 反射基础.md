---
title: Java 反射基础
created: 2026-05-04 21:00:00
category: Java-Spring
tags:
  - Java
  - Reflection
  - Spring-前置
---

<!-- markdownlint-disable MD025 -->

# Java 反射基础

## 为什么要学反射

上一节讲了注解是"贴在代码上的标签"。但标签贴上去之后，谁来读？答案是反射（Reflection）。

Spring 在启动时需要做这些事：扫描所有类、找到带特定注解的类、创建它们的实例、把依赖注入进去。这些操作在编译时是不确定的 -- Spring 不知道你的项目里有哪些类、哪些类需要被管理。它必须在运行时动态地"看一看"你的代码结构，然后做出决策。这个"在运行时看代码结构"的能力，就是反射。

你不需要精通反射的每个 API，但需要理解它能做什么、Spring 怎么用它。

---

## 核心概念

### 反射是什么

**Reflection（反射）是 Java 提供的一种机制，允许程序在运行时检查和操作类的结构（字段、方法、构造器、注解等）。**

正常写代码时，你在编译期就确定了要调用哪个类的哪个方法。反射打破了这个限制 -- 你可以在运行时通过字符串名称找到类、创建实例、调用方法。就像一面镜子，程序可以"照见"自己的结构并动态操作它。

### 为什么需要反射

框架的本质是"通用代码处理未知的用户代码"。Spring 不可能提前知道你会写哪些类，它必须在运行时动态发现和操作它们。反射让这成为可能 -- Spring 用反射扫描类、读注解、创建 Bean 实例、注入依赖。没有反射，就没有"自动"这个词。

### 没有反射会怎样

框架就无法做到"自动"。你需要手动注册每个类、手动创建每个对象、手动连接每个依赖。这正是早期 Java 开发的痛苦之处 -- 大量 XML 配置、大量样板代码，每新增一个类都要同步更新配置文件。有了反射，Spring 可以在启动时自动扫描、自动装配，你只需要写注解。

---

## 概念深入解释

### 反射的四项核心能力

**1. 获取类的信息**

在运行时拿到一个类的 `Class` 对象，从而获取它的所有结构信息：

```java
Class<?> clazz = Class.forName("com.example.UserService");
// 或者
Class<?> clazz = UserService.class;
// 或者
Class<?> clazz = userService.getClass();
```

`Class` 对象是反射的入口。有了它，你可以查询这个类的字段、方法、构造器、注解、父类、接口等一切信息。

**2. 创建实例**

不用 `new` 关键字，通过反射动态创建对象：

```java
Class<?> clazz = Class.forName("com.example.UserService");
Constructor<?> constructor = clazz.getDeclaredConstructor();
Object instance = constructor.newInstance();
```

Spring 创建 Bean 的底层机制就是这个 -- 通过反射调用构造器创建实例。

**3. 调用方法**

在运行时通过方法名调用方法：

```java
Method method = clazz.getMethod("findById", Long.class);
Object result = method.invoke(instance, 1L);
```

Spring AOP 的底层实现依赖这个能力 -- 在调用目标方法前后插入额外逻辑。

**4. 读写字段**

直接访问对象的字段，即使是 `private` 的：

```java
Field field = clazz.getDeclaredField("name");
field.setAccessible(true);  // 突破 private 限制
field.set(instance, "Alice");
```

Spring 的字段注入（`@Autowired` 直接标注在字段上）就是通过反射设置 private 字段的值。

### Spring 怎么用反射

把上面的能力串起来，Spring 启动时的核心流程大致是：

1. **组件扫描** -- 扫描指定包下的所有类，用反射获取每个类的 `Class` 对象
2. **注解检查** -- 检查类上是否有 `@Component`、`@Service` 等注解（反射读取注解）
3. **实例化** -- 对需要管理的类，通过反射调用构造器创建实例
4. **依赖注入** -- 检查构造器参数或字段上的 `@Autowired` 注解，通过反射注入依赖
5. **代理创建** -- 对需要 AOP 的类，通过 JDK 动态代理或 CGLIB 等机制创建代理对象

### `Class` 对象的关键 API

不需要记住所有 API，了解这几个就够理解 Spring 的行为：

| 方法 | 作用 | Spring 用途 |
|------|------|-------------|
| `getAnnotations()` | 获取类上的所有注解 | 组件扫描时判断类的角色 |
| `getDeclaredFields()` | 获取所有字段（含 private） | 字段注入 |
| `getDeclaredConstructors()` | 获取所有构造器 | 构造器注入、实例化 Bean |
| `getDeclaredMethods()` | 获取所有方法 | AOP 切面匹配 |
| `newInstance()` | 创建实例（已废弃，用 Constructor） | Bean 实例化 |

### 反射的代价

**性能开销。** 反射调用比直接调用慢。JVM 无法对反射调用做内联优化。不过 Spring 主要在启动阶段使用反射，运行时通过缓存和代理减少反射调用，实际影响有限。

**类型安全丧失。** 反射绕过了编译期类型检查。`method.invoke()` 返回的是 `Object`，类型错误只能在运行时发现。

**封装性破坏。** `setAccessible(true)` 可以突破 `private` 限制。在 Java 9+ 的模块系统中，跨模块的反射访问受到限制。

---

## 核心要点

1. **反射让程序在运行时动态检查和操作类的结构**，是框架实现"自动化"的基础。
2. **Spring 用反射做四件事：扫描类、读注解、创建实例、注入依赖。**
3. **`Class` 对象是反射的入口**，所有操作都从获取 `Class` 对象开始。
4. **反射有性能和类型安全的代价**，但框架层面已经做了优化，日常开发中不需要过度担心。
5. **你不需要自己写反射代码**，但理解它能帮你理解 Spring 的行为和排查问题。

---

## 常见误区

- **以为日常开发需要写反射代码。** 绝大多数情况下不需要。反射是框架的底层工具，应用开发者通过注解和 Spring API 间接使用它。只有在写框架、写通用工具库时才需要直接操作反射。
- **过度担心反射性能。** Spring 在启动时大量使用反射，但运行时通过 CGLIB 代理、缓存等手段避免了频繁的反射调用。启动慢一点是正常的，运行时性能不受影响。
- **忽略 `setAccessible` 的安全含义。** 在 Java 9+ 的模块系统中，跨模块的反射访问受到限制。如果你遇到 `InaccessibleObjectException`，通常是模块系统在阻止非法的反射访问。

---

## 与其他概念的关联

- **前置：** [Java 注解机制](./02_Java%20注解机制.md) -- 注解定义了元数据，反射负责在运行时读取这些元数据。两者配合使用。
- **并行：** [Java Lambda 与函数式接口](./04_Java%20Lambda%20与函数式接口.md) -- 同属 Part 1 准备阶段，Lambda 是代码风格层面的前置知识。
- **后续：** [Java Spring IoC](./05_Java%20Spring%20IoC.md) -- 反射是 Spring IoC 容器的底层实现基础。理解反射后，学习 IoC 和 Bean 时会更容易理解"Spring 是怎么做到自动管理对象的"。
