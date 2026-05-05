---
title: Java Spring Boot 项目结构
created: 2026-05-05 11:27:43
category: Java-Spring
tags:
  - Java
  - Spring
  - Spring-Boot
  - 项目结构
---

<!-- markdownlint-disable MD025 -->

# Java Spring Boot 项目结构

## 为什么要学项目结构

前面讲了 Spring Boot 的核心机制（自动配置、Starter），现在你已经能让项目跑起来了。但"能跑"和"好维护"是两回事。

Spring Boot 对项目结构有一些约定。这些约定不是强制的，但遵循它们能让组件扫描正常工作、让团队协作更顺畅、让项目规模增长时不至于混乱。理解这些约定，也能帮你理解为什么某些 Bean 没被扫描到。

---

## 核心概念

### 项目结构约定是什么

**Spring Boot 的项目结构约定是一组关于"代码放在哪里"的最佳实践，核心原则是：启动类放在根包，业务代码放在根包的子包中，按功能或领域组织。**

这不是框架强制的规则，而是"约定优于配置"理念的延伸。遵循约定，组件扫描自动生效；违反约定，你需要手动配置扫描范围。

### 为什么需要项目结构约定

`@SpringBootApplication` 默认扫描它所在包及所有子包。如果启动类在 `com.example.app`，那么 `com.example.app.user`、`com.example.app.order` 下的组件都会被自动扫描。这个约定让你不需要手动配置 `@ComponentScan` 的 basePackages。

### 没有项目结构约定会怎样

代码随意放置，组件扫描范围不确定。某些 Bean 可能因为不在扫描范围内而没有注册，启动时不报错但运行时报 `NoSuchBeanDefinitionException`。团队成员各自按习惯组织代码，项目结构混乱，新人上手困难。

---

## 概念深入解释

### 标准 Maven/Gradle 目录结构

```
project-root/
├── src/
│   ├── main/
│   │   ├── java/          (Java 源码)
│   │   └── resources/     (配置文件、静态资源)
│   │       ├── application.yml
│   │       ├── static/    (静态文件)
│   │       └── templates/ (模板文件)
│   └── test/
│       ├── java/          (测试代码)
│       └── resources/     (测试配置)
├── pom.xml (Maven) 或 build.gradle (Gradle)
└── README.md
```

这是 Maven/Gradle 的标准目录结构，Spring Boot 遵循它。`src/main/resources` 下的 `application.yml` 会被自动加载为配置文件。

### 两种包组织方式

**按领域/功能组织（推荐）：**

```
com.example.app/
├── Application.java          (启动类，在根包)
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   ├── UserRepository.java
│   └── User.java            (Entity)
├── order/
│   ├── OrderController.java
│   ├── OrderService.java
│   ├── OrderRepository.java
│   └── Order.java
└── common/
    ├── exception/
    │   └── GlobalExceptionHandler.java
    └── config/
        └── SecurityConfig.java
```

优点：相关代码聚合在一起，改一个功能只需要看一个包。适合中大型项目，也为未来微服务拆分做准备。

**按技术层组织：**

```
com.example.app/
├── Application.java
├── controller/
│   ├── UserController.java
│   └── OrderController.java
├── service/
│   ├── UserService.java
│   └── OrderService.java
├── repository/
│   ├── UserRepository.java
│   └── OrderRepository.java
└── entity/
    ├── User.java
    └── Order.java
```

优点：结构简单直观，适合小型项目。缺点：改一个功能需要跨多个包，功能之间的边界不清晰。

### 启动类的位置

启动类必须放在根包（所有业务代码的公共父包）：

```java
package com.example.app;  // 根包

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

如果启动类在 `com.example.app`，那么 `com.example.app.*` 下的所有组件都会被扫描。如果你把启动类放在 `com.example.app.config` 下，`com.example.app.user` 就不在扫描范围内了。

### 配置类的组织

配置类（`@Configuration`）通常放在 `config` 包或对应功能包下：

```
com.example.app/
├── config/
│   ├── SecurityConfig.java      (安全配置)
│   ├── WebMvcConfig.java        (Web MVC 定制)
│   └── CacheConfig.java        (缓存配置)
```

配置类的作用是覆盖自动配置的默认行为，或者定义第三方库的 Bean。

### resources 目录结构

```
src/main/resources/
├── application.yml              (主配置)
├── application-dev.yml          (开发环境配置)
├── application-prod.yml         (生产环境配置)
├── static/                      (静态资源，直接映射为 URL)
│   ├── css/
│   └── js/
├── templates/                   (模板文件，Thymeleaf 等)
└── db/
    └── migration/               (数据库迁移脚本，Flyway/Liquibase)
```

### 多模块项目结构

当项目规模增大，可以拆分为多个 Maven/Gradle 模块：

```
parent/
├── pom.xml                      (父 POM，管理版本)
├── app-api/                     (Web 层，启动类在这里)
│   └── src/main/java/
├── app-service/                 (业务逻辑层)
│   └── src/main/java/
├── app-dao/                     (数据访问层)
│   └── src/main/java/
└── app-common/                  (公共工具、DTO)
    └── src/main/java/
```

模块间通过 Maven 依赖关系连接。启动类所在模块依赖其他所有模块。

---

## 核心要点

1. **启动类放在根包。** 这是组件扫描正常工作的前提，违反这个约定是最常见的"Bean 找不到"原因。
2. **推荐按领域/功能组织包结构。** 相关代码聚合，改一个功能只看一个包，也为未来拆分做准备。
3. **`src/main/resources/application.yml` 是默认配置文件位置。** Spring Boot 自动加载，不需要额外配置。
4. **配置类集中在 `config` 包。** 让团队成员快速找到所有定制配置。
5. **项目规模增大时考虑多模块。** 通过 Maven/Gradle 模块实现编译期的依赖隔离。

---

## 常见误区

- **启动类放在子包中导致部分组件未被扫描。** 启动类在 `com.example.app.config`，但 Service 在 `com.example.app.service`，两者是兄弟包而非父子包，Service 不会被扫描。解决：把启动类移到 `com.example.app`。
- **在根包下堆积大量类。** 根包应该只有启动类（和少量全局配置）。业务代码应该放在子包中。根包堆积太多类会让项目结构混乱。
- **测试代码的包结构与主代码不一致。** 测试类应该和被测试类在相同的包路径下（`src/test/java` 中的包结构镜像 `src/main/java`）。这样测试类可以访问包级别可见的方法。
- **把所有配置写在一个巨大的配置类中。** 应该按职责拆分：安全配置、Web 配置、缓存配置各自独立。一个配置类只负责一个关注点。

---

## 与其他概念的关联

- **前置：** [Java Spring Boot 概述](./10_Java%20Spring%20Boot%20概述.md) -- 理解了 Spring Boot 是什么之后，需要知道项目怎么组织。[Java Spring 容器](./09_Java%20Spring%20容器.md) -- 组件扫描是容器启动流程的一部分，项目结构直接影响扫描范围。
- **并行：** [Java Spring Boot 配置](./14_Java%20Spring%20Boot%20配置.md) -- 项目结构决定了配置文件放在哪里，配置文件决定了应用的运行行为。
- **后续：** Part 4 Web 开发 -- 项目结构确定后，开始在 Controller、Service、Repository 各层写代码。
