---
title: Java Spring Boot 概述
created: 2026-05-05 11:27:43
category: Java-Spring
tags:
  - Java
  - Spring
  - Spring-Boot
---

<!-- markdownlint-disable MD025 -->

# Java Spring Boot 概述

## 为什么要学 Spring Boot

Part 2 讲完了 IoC、Bean、DI、AOP、容器 -- 这些是 Spring 的核心哲学和机制。但如果你现在就去创建一个纯 Spring 项目，你会发现：要手动配置 DispatcherServlet、手动配置数据源、手动配置视图解析器、手动配置事务管理器... 光是让项目跑起来就要写一堆配置代码。

Spring Boot 解决的就是这个问题：**让你用 Spring 的全部能力，但不用操心配置。**

理解了 Part 2 的核心概念后，Spring Boot 就是把这些概念"落地"的工具。它不引入新的编程模型，只是让已有的模型更容易使用。

---

## 核心概念

### Spring Boot 是什么

**Spring Boot 是 Spring 框架的"脚手架"层，通过自动配置、内嵌服务器和 Starter 依赖，让你无需手动配置就能快速启动一个生产级 Spring 应用。**

类比：Spring 是一套完整的厨房设备（烤箱、灶台、冰箱），Spring Boot 是预设好温度和时间的"一键烹饪"模式。设备还是那些设备，但你不需要每次都手动调参数。

Spring Boot 不是一个独立的框架，它是 Spring 之上的一层便利封装。底层仍然是 IoC 容器、Bean、DI、AOP -- 只是配置过程被自动化了。

### 为什么需要 Spring Boot

纯 Spring 项目的配置量巨大。一个简单的 Web 应用需要：配置 DispatcherServlet、配置 ViewResolver、配置 DataSource、配置 TransactionManager、配置 Jackson 序列化、部署到外部 Tomcat... 每个项目都重复这些步骤。Spring Boot 通过"约定优于配置"的理念，把 80% 的常见配置自动完成，你只需要关注 20% 的业务特定配置。

### 没有 Spring Boot 会怎样

你需要手动编写大量样板配置（XML 或 Java Config），手动管理依赖版本兼容性，手动配置和部署外部应用服务器。每个新项目的启动成本高，团队成员需要记住大量配置细节。有了 Spring Boot，创建一个可运行的 Web 应用只需要一个依赖（`spring-boot-starter-web`）和一个启动类。

---

## 概念深入解释

### Spring vs Spring Boot 对比

| 维度 | Spring | Spring Boot |
|------|--------|-------------|
| 定位 | 基础框架，提供核心能力 | 脚手架，简化 Spring 的使用 |
| 配置方式 | 手动配置（XML / Java Config） | 自动配置 + 少量覆盖 |
| 服务器 | 需要外部应用服务器（Tomcat、Jetty） | 内嵌服务器，`java -jar` 直接运行 |
| 依赖管理 | 手动管理每个依赖的版本 | Starter + BOM 统一管理版本 |
| 启动速度 | 项目搭建慢，运行时无差异 | 项目搭建快，运行时无差异 |
| 适用场景 | 需要极度定制化的场景 | 绝大多数场景（推荐默认选择） |

关键认知：**Spring Boot 不是 Spring 的替代品，而是 Spring 的增强层。** 你在 Spring Boot 中写的代码，本质上就是 Spring 代码。`@Service`、`@Autowired`、`@Transactional` 这些注解的行为完全一样。

### Spring Boot 的四大核心特性

**1. 自动配置（Auto-Configuration）**

Spring Boot 根据 classpath 中存在的依赖，自动配置相应的 Bean。比如：

- 检测到 `spring-boot-starter-web` → 自动配置内嵌 Tomcat + DispatcherServlet
- 检测到 `spring-boot-starter-data-jpa` + H2 驱动 → 自动配置内存数据库 + EntityManagerFactory
- 检测到 `spring-boot-starter-security` → 自动配置基础安全过滤器链

你不需要写任何配置代码，引入依赖就自动生效。

**2. 内嵌服务器（Embedded Server）**

传统 Spring 应用需要打包为 WAR，部署到外部 Tomcat。Spring Boot 把 Tomcat（或 Jetty、Undertow）内嵌到应用中，打包为可执行 JAR，`java -jar app.jar` 直接运行。

这意味着：开发时不需要安装和配置 Tomcat，部署时不需要应用服务器，容器化时镜像更简单。

**3. Starter 依赖**

Starter 是一组预定义的依赖集合。引入一个 Starter 就等于引入了一类功能所需的所有库，且版本已经过兼容性测试。

```xml
<!-- 一个依赖搞定 Web 开发 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

不需要手动引入 Spring MVC、Jackson、Tomcat、Validation 等十几个依赖并逐一确认版本兼容。

**4. 生产就绪特性（Production-Ready Features）**

Spring Boot Actuator 提供健康检查、指标暴露、环境信息等运维端点。应用跑起来后，你立刻就能知道它是否健康、性能如何。

### @SpringBootApplication 组合注解

Spring Boot 应用的启动类上只需要一个注解：

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

`@SpringBootApplication` 是三个注解的组合：

| 组成注解 | 作用 |
|----------|------|
| `@SpringBootConfiguration` | 标记为配置类（包含 `@Configuration`） |
| `@EnableAutoConfiguration` | 启用自动配置机制 |
| `@ComponentScan` | 启用组件扫描（默认扫描当前包及子包） |

这就是为什么一个注解就能让整个应用跑起来 -- 它同时声明了"这是配置类"、"启用自动配置"、"扫描组件"。

### Spring Boot 的版本演进

| 版本 | 关键变化 |
|------|----------|
| Spring Boot 2.x | 基于 Spring 5，Java 8+，`spring.factories` 加载自动配置 |
| Spring Boot 3.x | 基于 Spring 6，Java 17+，Jakarta EE（`javax.*` → `jakarta.*`），GraalVM 原生支持，自动配置索引改为 `.imports` 文件 |

如果你是新项目，直接用 Spring Boot 3.x。

---

## 核心要点

1. **Spring Boot 是 Spring 的便利层，不是替代品。** 底层仍然是 IoC、Bean、DI、AOP，只是配置被自动化了。
2. **"约定优于配置"是核心理念。** 80% 的场景用默认配置就够了，只需要覆盖你需要定制的部分。
3. **一个 Starter + 一个启动类就能跑起来。** 这是 Spring Boot 的核心价值 -- 极低的启动成本。
4. **自动配置不是黑魔法。** 它只是根据 classpath 中的依赖，有条件地注册 Bean。理解这一点能帮你排查问题。
5. **内嵌服务器简化了开发和部署。** 不再需要外部 Tomcat，`java -jar` 就是生产部署方式。

---

## 常见误区

- **以为 Spring Boot 是一个全新的框架。** Spring Boot 不引入新的编程模型。你写的 `@Service`、`@RestController`、`@Transactional` 跟纯 Spring 完全一样。Spring Boot 只是帮你省去了配置步骤。
- **启动类放错位置导致组件扫描失败。** `@SpringBootApplication` 默认扫描它所在包及子包。如果启动类在 `com.example` 包，而你的 Service 在 `com.other` 包，就不会被扫描到。启动时不报错，但注入时报 `NoSuchBeanDefinitionException`。
- **以为自动配置不可覆盖。** 自动配置的优先级低于你手动定义的 Bean。如果你显式声明了一个 `DataSource` Bean，自动配置的 DataSource 就不会生效。这是 `@ConditionalOnMissingBean` 的作用。
- **混淆 Spring Boot 版本和 Spring 版本。** Spring Boot 3.x 基于 Spring 6，Spring Boot 2.x 基于 Spring 5。升级 Spring Boot 大版本意味着底层 Spring 也升级了，可能涉及 API 变化（如 `javax` → `jakarta`）。

---

## 与其他概念的关联

- **前置：** [Java Spring IoC](./05_Java%20Spring%20IoC.md) -- Spring Boot 的自动配置本质上是自动向 IoC 容器注册 Bean。[Java Spring Bean](./06_Java%20Spring%20Bean.md) -- 自动配置创建的就是 Bean。[Java Spring 容器](./09_Java%20Spring%20容器.md) -- Spring Boot 启动时创建的 ApplicationContext 就是容器。
- **并行：** [Java Spring Boot 自动配置](./11_Java%20Spring%20Boot%20自动配置.md) -- 自动配置是 Spring Boot 最核心的机制，本文概述，下一篇深入。[Java Spring Boot Starter](./12_Java%20Spring%20Boot%20Starter.md) -- Starter 是自动配置的触发器。
- **后续：** [Java Spring Boot 项目结构](./13_Java%20Spring%20Boot%20项目结构.md) -- 理解了 Spring Boot 是什么之后，需要知道项目怎么组织。[Java Spring Boot 配置](./14_Java%20Spring%20Boot%20配置.md) -- 自动配置覆盖不了的部分，通过配置文件定制。[Java Spring 可观测性](./15_Java%20Spring%20可观测性.md) -- 应用跑起来后，需要知道它的运行状态。
