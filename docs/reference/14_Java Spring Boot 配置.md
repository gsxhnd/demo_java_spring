---
title: Java Spring Boot 配置
created: 2026-05-05 11:27:43
category: Java-Spring
tags:
  - Java
  - Spring
  - Spring-Boot
  - Configuration
  - Profile
---

<!-- markdownlint-disable MD025 -->

# Java Spring Boot 配置

## 为什么要学配置

前面讲了自动配置帮你注册 Bean，Starter 帮你管理依赖。但自动配置的默认值不一定适合你的场景 -- 数据库地址、服务端口、日志级别、连接池大小... 这些都需要你来指定。

Spring Boot 的配置体系就是用来做这件事的：**在自动配置的基础上，微调应用的行为。**

配置体系还解决了另一个关键问题：多环境管理。开发环境连本地数据库，测试环境连测试库，生产环境连生产库 -- 同一份代码，不同环境的配置不同。Profile 机制让你优雅地处理这个问题。

---

## 核心概念

### Spring Boot 配置是什么

**Spring Boot 配置是一套外部化的属性管理体系，通过 `application.yml`（或 `.properties`）文件、环境变量、命令行参数等方式，控制应用的运行行为。**

配置的本质是键值对。Spring Boot 把这些键值对绑定到自动配置类的属性上，从而改变自动配置的默认行为。比如 `server.port=9090` 会让内嵌 Tomcat 监听 9090 端口而不是默认的 8080。

### 为什么需要配置体系

应用的行为不应该硬编码在代码中。数据库地址、密钥、端口号、超时时间 -- 这些值在不同环境中不同，在不同部署中不同。配置体系让你把这些可变的值从代码中抽离出来，通过外部文件或环境变量注入，实现"一次构建，多处部署"。

### 没有配置体系会怎样

你需要为每个环境维护一份代码（或者在代码中写 `if (env == "prod")` 这样的判断），每次部署都要改代码重新编译。有了配置体系，代码只写一份，不同环境通过不同的配置文件或环境变量控制行为。

---

## 概念深入解释

### 配置文件格式

Spring Boot 支持两种格式：

**YAML 格式（推荐）：** 层次清晰，适合复杂配置

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: secret
  jpa:
    hibernate:
      ddl-auto: update
```

**Properties 格式：** 简单直接，适合少量配置

```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
```

两种格式功能等价，选择一种即可。YAML 在配置项多时可读性更好。

### 配置的加载优先级

Spring Boot 从多个来源加载配置，优先级从高到低：

| 优先级 | 来源 | 典型用途 |
|--------|------|----------|
| 1（最高） | 命令行参数 `--server.port=9090` | 临时覆盖 |
| 2 | 环境变量 `SERVER_PORT=9090` | 容器化部署 |
| 3 | `application-{profile}.yml` | 环境特定配置 |
| 4 | `application.yml` | 通用默认配置 |
| 5（最低） | 自动配置的默认值 | 框架兜底 |

高优先级的配置会覆盖低优先级的同名配置。这意味着你可以在 `application.yml` 中写默认值，在部署时通过环境变量覆盖敏感信息（如数据库密码）。

### Profile（多环境配置）

Profile 是 Spring Boot 管理多环境配置的机制。

**定义 Profile 配置文件：**

```
src/main/resources/
├── application.yml           (通用配置)
├── application-dev.yml       (开发环境)
├── application-test.yml      (测试环境)
└── application-prod.yml      (生产环境)
```

**激活 Profile：**

```yaml
# application.yml 中指定默认 Profile
spring:
  profiles:
    active: dev
```

或通过命令行：`java -jar app.jar --spring.profiles.active=prod`

或通过环境变量：`SPRING_PROFILES_ACTIVE=prod`

**Profile 配置会与主配置合并：** `application-dev.yml` 中的配置会覆盖 `application.yml` 中的同名配置，未覆盖的配置保持不变。

### @ConfigurationProperties（类型安全绑定）

`@Value` 注入单个配置值简单但不够结构化。`@ConfigurationProperties` 把一组配置绑定到一个 Java 对象上：

```yaml
# application.yml
app:
  name: MyApplication
  upload:
    max-size: 10MB
    allowed-types:
      - image/png
      - image/jpeg
```

```java
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String name;
    private Upload upload;

    public static class Upload {
        private String maxSize;
        private List<String> allowedTypes;
        // getters and setters
    }
    // getters and setters
}
```

优点：类型安全（编译期检查）、IDE 自动补全、集中管理、支持校验（`@Validated`）。

### 配置值的注入方式对比

| 方式 | 适用场景 | 示例 |
|------|----------|------|
| `@Value("${key}")` | 注入单个值 | `@Value("${server.port}")` |
| `@ConfigurationProperties` | 注入一组结构化配置 | 绑定 `app.*` 到对象 |
| `Environment` 对象 | 动态获取配置 | `env.getProperty("key")` |

推荐：少量简单值用 `@Value`，结构化配置用 `@ConfigurationProperties`。

### 配置的常见模式

**占位符引用：**

```yaml
app:
  name: MyApp
  description: ${app.name} is running on port ${server.port}
```

**默认值：**

```yaml
# YAML 中没有直接的默认值语法，但 @Value 支持
# @Value("${app.name:DefaultApp}")
```

**随机值（测试用）：**

```yaml
app:
  secret: ${random.uuid}
  port: ${random.int[8000,9000]}
```

---

## 核心要点

1. **配置的本质是"微调自动配置的默认行为"。** 自动配置提供合理默认值，配置文件覆盖你需要定制的部分。
2. **Profile 解决多环境问题。** 一份代码，通过激活不同 Profile 适配不同环境。
3. **配置有优先级。** 命令行 > 环境变量 > Profile 配置 > 主配置 > 自动配置默认值。
4. **结构化配置用 `@ConfigurationProperties`。** 类型安全、IDE 友好、集中管理。
5. **敏感信息不要写在代码仓库的配置文件中。** 通过环境变量或外部配置中心注入。

---

## 常见误区

- **在 `application.yml` 中写死生产环境的数据库密码并提交到 Git。** 敏感信息应该通过环境变量注入（`spring.datasource.password=${DB_PASSWORD}`），或使用配置中心（如 Vault、Nacos Config）。
- **YAML 缩进错误导致配置不生效。** YAML 对缩进敏感，必须用空格（不能用 Tab）。缩进层级错误会导致配置被解析到错误的层级，表现为"配置写了但没生效"。
- **Profile 配置文件命名错误。** 必须是 `application-{profile}.yml` 格式。`application_dev.yml`（下划线）或 `dev-application.yml`（顺序反了）都不会被识别。
- **以为 `@Value` 可以注入复杂对象。** `@Value` 只能注入简单类型（String、int、boolean 等）和简单集合。复杂嵌套结构应该用 `@ConfigurationProperties`。

---

## 与其他概念的关联

- **前置：** [Java Spring Boot 自动配置](./11_Java%20Spring%20Boot%20自动配置.md) -- 配置文件的作用是覆盖自动配置的默认值。理解自动配置后，才知道哪些配置项可以调整。[Java Spring DI](./07_Java%20Spring%20DI.md) -- `@Value` 本质上是 DI 的一种形式，注入的是配置值而非 Bean。
- **并行：** [Java Spring Boot 项目结构](./13_Java%20Spring%20Boot%20项目结构.md) -- 配置文件的位置由项目结构决定（`src/main/resources/`）。
- **后续：** [Java Spring 可观测性](./15_Java%20Spring%20可观测性.md) -- Actuator 的端点暴露、指标采集等行为都通过配置文件控制。Part 6 的配置管理与 Profile 会更深入地讨论多环境配置策略。
