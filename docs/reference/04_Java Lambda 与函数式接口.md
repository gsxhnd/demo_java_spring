---
title: Java Lambda 与函数式接口
created: 2026-05-04 21:00:00
category: Java-Spring
tags:
  - Java
  - Lambda
  - FunctionalInterface
  - Spring-前置
---

<!-- markdownlint-disable MD025 -->

# Java Lambda 与函数式接口

## 为什么要学 Lambda

前两篇讲了注解和反射 -- 它们是 Spring 在"配置"和"底层机制"层面的前置知识。Lambda 则是在"代码风格"层面的前置知识。

Spring 5 开始大量使用 Lambda 表达式，尤其是在以下场景：

- Spring Security 的配置（链式调用 + Lambda）
- Spring WebFlux（响应式编程，全程 Lambda）
- 自定义 Bean 定义、条件判断
- Stream API 处理集合数据

如果你看到 `http -> http.csrf(csrf -> csrf.disable())` 这样的代码觉得困惑，那就需要先理解 Lambda。

---

## 核心概念

### Lambda 是什么

**Lambda 表达式是匿名函数的简写语法，Java 8 引入。** 它让你可以把一段行为（函数）当作参数传递，而不需要定义一个完整的类。

传统写法 -- 用匿名内部类实现一个接口：

```java
Runnable task = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};
```

Lambda 写法：

```java
Runnable task = () -> System.out.println("Hello");
```

两者在这个例子中的行为等价，但实现机制不同。匿名内部类通常会生成独立的类；Lambda 通过 `invokedynamic` 和运行时引导机制绑定到目标函数式接口，不只是简单地改写成匿名内部类。

### 为什么需要 Lambda

Spring 5+ 大量使用 Lambda 做配置 DSL（如 Spring Security 的链式配置）、回调处理、集合数据转换。Lambda 让代码更简洁、更声明式。没有 Lambda，同样的逻辑需要用匿名内部类实现，代码量翻倍且可读性差。

### 没有 Lambda 会怎样

每个回调都要写一个匿名内部类，代码冗长且噪音多。现代 Spring 的配置代码（Security、WebFlux、Stream API）几乎不可能用匿名内部类写得优雅。Lambda 是读懂和写好现代 Spring 代码的必备语法基础。

---

## 概念深入解释

### Lambda 的语法

Lambda 的基本形式是 `(参数) -> 表达式` 或 `(参数) -> { 语句块 }`：

```java
// 无参数
() -> System.out.println("hello")

// 单参数（可省略括号）
name -> System.out.println(name)

// 多参数
(a, b) -> a + b

// 多行语句
(a, b) -> {
    int sum = a + b;
    return sum;
}
```

类型推断：编译器根据上下文推断参数类型，大多数情况下不需要显式声明。

### 函数式接口（Functional Interface）

**函数式接口是只有一个抽象方法的接口。** Lambda 表达式的本质就是函数式接口的实例。

Java 是强类型语言，Lambda 不能独立存在，它必须有一个"目标类型"。函数式接口就是这个目标类型 -- 它定义了 Lambda 的参数和返回值签名。

Java 用 `@FunctionalInterface` 注解标记函数式接口（可选，但推荐加上，编译器会帮你检查）：

```java
@FunctionalInterface
public interface Converter<F, T> {
    T convert(F from);
}

Converter<String, Integer> toInt = Integer::parseInt;
```

### 四大内置函数式接口

`java.util.function` 包提供了一组通用的函数式接口，覆盖了最常见的场景：

| 接口 | 方法签名 | 用途 | Spring 使用场景 |
|------|----------|------|-----------------|
| `Function<T, R>` | `R apply(T t)` | 转换：T → R | 对象转 DTO |
| `Consumer<T>` | `void accept(T t)` | 消费：T → void | 配置回调（`http.cors(cors -> ...)`） |
| `Supplier<T>` | `T get()` | 供给：void → T | 延迟创建对象 |
| `Predicate<T>` | `boolean test(T t)` | 判断：T → boolean | 过滤条件 |

这四个接口是基础，其他接口（`BiFunction`、`UnaryOperator` 等）都是它们的变体。

### 方法引用（Method Reference）

方法引用是 Lambda 的进一步简写。当 Lambda 体只是调用一个已有方法时，可以用 `::` 语法：

```java
// Lambda
list.forEach(item -> System.out.println(item));
// 方法引用
list.forEach(System.out::println);
```

四种方法引用形式：

| 形式 | 语法 | 等价 Lambda |
|------|------|-------------|
| 静态方法 | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| 实例方法（特定对象） | `System.out::println` | `x -> System.out.println(x)` |
| 实例方法（任意对象） | `String::length` | `s -> s.length()` |
| 构造器 | `ArrayList::new` | `() -> new ArrayList<>()` |

### Lambda 与 Spring 的关系

Spring 中 Lambda 最典型的使用场景是**配置 DSL**。以 Spring Security 为例：

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http)
        throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**").permitAll()
            .anyRequest().authenticated()
        )
        .build();
}
```

这里每个 Lambda 都是一个 `Customizer<T>` 函数式接口的实例。Spring 把配置拆成了一个个小的 Lambda 回调，让配置代码更加声明式和可组合。

---

## 核心要点

1. **Lambda 是匿名函数的简写**，本质是函数式接口的实例。
2. **函数式接口 = 只有一个抽象方法的接口**，是 Lambda 的类型载体。
3. **`Function`、`Consumer`、`Supplier`、`Predicate` 是四个最核心的内置函数式接口。**
4. **方法引用（`::`）是 Lambda 的进一步简写**，当 Lambda 体只调用一个方法时使用。
5. **Spring 5+ 大量使用 Lambda 做配置 DSL**，理解 Lambda 才能读懂现代 Spring 代码。

---

## 常见误区

- **以为 Lambda 是新的语法特性，很复杂。** Lambda 的使用概念接近匿名内部类的简写，但 JVM 层面的实现机制不同：Lambda 通过 `invokedynamic` 指令和运行时引导机制绑定实现，通常不会像匿名内部类那样为每个表达式生成独立的 `.class` 文件；选择 Lambda 的主要理由是可读性和表达力，而不是绝对性能优势。
- **搞不清 Lambda 的类型。** Lambda 本身没有类型，它的类型由上下文决定（目标类型推断）。同一个 `x -> x + 1` 在不同上下文中可能是 `Function<Integer, Integer>`，也可能是 `UnaryOperator<Integer>`。
- **在 Lambda 中修改外部变量。** Lambda 可以捕获外部变量，但要求变量是 effectively final（事实上不可变）。如果你需要在 Lambda 中修改状态，应该用 `AtomicReference` 或其他线程安全的容器。
- **过度使用方法引用。** 方法引用在简单场景下很优雅，但如果需要额外的参数转换或逻辑，Lambda 更清晰。可读性优先。

---

## 与其他概念的关联

- **前置：** [Java 注解机制](./02_Java%20注解机制.md)、[Java 反射基础](./03_Java%20反射基础.md) -- 注解和反射是 Spring 的配置和底层机制基础，Lambda 是代码风格基础。三者共同构成 Spring 的语言层前置知识。
- **并行：** 无。这是 Part 1 的最后一个主题。
- **后续：** [Java Spring IoC](./05_Java%20Spring%20IoC.md) -- Part 1 准备阶段结束，接下来进入 Part 2 核心概念。Lambda 在 Spring Security 配置、WebFlux、Stream 数据处理中大量使用。
