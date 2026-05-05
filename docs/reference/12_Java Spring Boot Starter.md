---
title: Java Spring Boot Starter
created: 2026-05-05 11:27:43
category: Java-Spring
tags:
  - Java
  - Spring
  - Spring-Boot
  - Starter
---

<!-- markdownlint-disable MD025 -->

# Java Spring Boot Starter

## 为什么要学 Starter

上一节讲了自动配置的原理 -- Spring Boot 根据 classpath 中的依赖有条件地注册 Bean。那问题来了：classpath 中的依赖是怎么来的？谁来保证引入的依赖版本互相兼容？

答案是 Starter。

Starter 是 Spring Boot 依赖管理的核心机制。它解决了 Java 项目中一个长期痛点：**依赖地狱（Dependency Hell）**。一个功能可能需要引入 5-10 个库，每个库有自己的版本，版本之间可能冲突。Starter 把这些打包成一个依赖，版本由 Spring Boot 团队统一测试和维护。

---

## 核心概念

### Starter 是什么

**Starter 是一组预定义的依赖描述符（POM），引入一个 Starter 就等于引入了实现某类功能所需的全部依赖，且版本已经过兼容性测试。**

Starter 本身不包含代码，它只是一个 `pom.xml`，里面声明了一组传递依赖。比如 `spring-boot-starter-web` 会传递引入 Spring MVC、Jackson、Tomcat、Validation 等库。

类比：Starter 就像超市的"火锅套餐" -- 你不需要分别挑选底料、肉片、蔬菜、蘸料，买一个套餐就全齐了，而且搭配已经过验证。

### 为什么需要 Starter

Java 生态的依赖关系极其复杂。一个 Web 应用可能需要：Spring MVC、嵌入式 Tomcat、Jackson JSON、Bean Validation、SLF4J、Logback... 手动管理这些依赖的版本兼容性是噩梦。Starter 把"选择正确的依赖组合"这件事交给 Spring Boot 团队，你只需要声明"我要做 Web 开发"。

### 没有 Starter 会怎样

你需要手动引入每个库并逐一确认版本兼容。Spring MVC 5.3.x 需要搭配哪个版本的 Jackson？Hibernate 6.x 需要哪个版本的 Jakarta Persistence API？这些问题在没有 Starter 时需要你自己查文档、试错。有了 Starter，Spring Boot 的 BOM（Bill of Materials）统一管理了几百个库的版本，你只需要声明 Spring Boot 的版本号。

---

## 概念深入解释

### 常用 Starter 一览

| Starter | 功能 | 传递引入的核心依赖 |
|---------|------|-------------------|
| `spring-boot-starter-web` | Web 开发 | Spring MVC, Tomcat, Jackson, Validation |
| `spring-boot-starter-data-jpa` | JPA 数据访问 | Hibernate, Spring Data JPA, HikariCP |
| `spring-boot-starter-security` | 安全 | Spring Security |
| `spring-boot-starter-test` | 测试 | JUnit 5, Mockito, AssertJ, Spring Test |
| `spring-boot-starter-actuator` | 运维监控 | Micrometer, Health Indicators |
| `spring-boot-starter-validation` | 参数校验 | Hibernate Validator |
| `spring-boot-starter-cache` | 缓存抽象 | Spring Cache |
| `spring-boot-starter-mail` | 邮件发送 | Jakarta Mail |

### Starter 的命名规则

**官方 Starter：** `spring-boot-starter-{功能名}`

- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`

**第三方 Starter：** `{项目名}-spring-boot-starter`

- `mybatis-spring-boot-starter`
- `druid-spring-boot-starter`
- `knife4j-spring-boot-starter`

这个命名约定帮助你区分官方维护和社区维护的 Starter。

### Starter 与自动配置的关系

Starter 和自动配置是一对搭档：

```
Starter（引入依赖）→ classpath 中出现特定类 → 自动配置条件满足 → Bean 注册
```

具体来说：

1. 你在 `pom.xml` 中引入 `spring-boot-starter-data-jpa`
2. 这个 Starter 传递引入了 Hibernate、Spring Data JPA 等库
3. classpath 中出现了 `EntityManager`、`DataSource` 等类
4. `DataSourceAutoConfiguration`、`HibernateJpaAutoConfiguration` 的 `@ConditionalOnClass` 条件满足
5. 自动配置注册 DataSource、EntityManagerFactory、TransactionManager 等 Bean

Starter 负责"把材料准备好"，自动配置负责"把材料组装起来"。

### BOM（Bill of Materials）

Spring Boot 通过 `spring-boot-dependencies` BOM 统一管理所有依赖的版本。当你声明了 Spring Boot 的版本（如 3.2.0），BOM 就确定了几百个库的兼容版本。

在 Maven 中体现为 `<parent>`：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>
```

之后引入 Starter 时不需要写版本号：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <!-- 版本由 parent 的 BOM 管理，不需要写 -->
</dependency>
```

### 排除不需要的传递依赖

有时 Starter 引入的某个依赖你不需要，或者想替换为其他实现：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <!-- 排除 Tomcat，改用 Undertow -->
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-undertow</artifactId>
</dependency>
```

### 自定义 Starter

当你的团队有通用的基础设施代码（如统一的日志格式、统一的异常处理、公司内部的 SDK 封装），可以把它们打包成自定义 Starter，让其他项目一键引入。

自定义 Starter 的结构：

```
my-spring-boot-starter/
├── pom.xml                    (声明传递依赖)
├── src/main/java/
│   └── MyAutoConfiguration.java  (自动配置类)
└── src/main/resources/
    └── META-INF/spring/
        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

---

## 核心要点

1. **Starter = 依赖集合 + 版本管理。** 它本身不包含代码，只是把正确的依赖组合打包在一起。
2. **Starter 和自动配置是搭档。** Starter 引入依赖，自动配置根据依赖注册 Bean。
3. **BOM 统一管理版本。** 你只需要声明 Spring Boot 版本，所有依赖的版本自动确定。
4. **官方 Starter 用 `spring-boot-starter-*` 命名，第三方用 `*-spring-boot-starter`。**
5. **可以排除不需要的传递依赖。** 用 Maven 的 `<exclusions>` 或 Gradle 的 `exclude` 实现。

---

## 常见误区

- **以为 Starter 包含业务代码。** Starter 只是依赖描述符（POM），不包含 Java 代码。真正的逻辑在它传递引入的库和对应的自动配置类中。
- **手动指定 Starter 中依赖的版本。** 如果你在 `pom.xml` 中显式指定了某个库的版本，可能会覆盖 BOM 管理的版本，导致版本冲突。除非你确定需要特定版本，否则让 BOM 管理。
- **引入了 Starter 但功能没生效。** 常见原因：缺少必要的配置属性（如 `spring-boot-starter-data-jpa` 需要配置 `spring.datasource.url`），或者自动配置被排除了。检查启动日志中的条件评估报告。
- **混淆 `spring-boot-starter` 和 `spring-boot-starter-*`。** `spring-boot-starter`（不带后缀）是核心 Starter，只包含 Spring Boot 核心 + 自动配置 + 日志。它是所有其他 Starter 的基础依赖。

---

## 与其他概念的关联

- **前置：** [Java Spring Boot 概述](./10_Java%20Spring%20Boot%20概述.md) -- Starter 是 Spring Boot 四大核心特性之一。[Java Spring Boot 自动配置](./11_Java%20Spring%20Boot%20自动配置.md) -- Starter 引入依赖后，自动配置根据依赖注册 Bean。两者是"材料"和"组装"的关系。
- **并行：** [Java Spring Boot 配置](./14_Java%20Spring%20Boot%20配置.md) -- Starter + 自动配置提供默认行为，配置文件用于微调。
- **后续：** [Java Spring Boot 项目结构](./13_Java%20Spring%20Boot%20项目结构.md) -- 理解了依赖管理后，需要知道项目代码怎么组织。
