# Spring Boot 自动配置与 Starter 机制 / Auto-Configuration & Starter

> Spring Boot 的"约定优于配置"魔法背后，是一套精密的自动配置机制。理解它，才能在需要时精准调优和自定义。

## 1. 概述 / Overview

Spring Boot 通过自动配置（Auto-Configuration）根据 classpath 中的依赖、已有 Bean 和配置属性，自动注册合适的 Bean。Starter 则是一组预定义的依赖集合 + 自动配置，实现"引入依赖即可用"的开发体验。

## 2. 术语表 / Glossary

> 以下术语是理解 Spring Boot 自动配置的前提。如果不熟悉"Bean"等基础概念，建议先阅读 [IoC 与依赖注入](01-ioc-di.md)。

| 术语 | 定义 | 作用 | 为什么存在 |
|------|------|------|-----------|
| **Bean** | 由 Spring IoC 容器管理的 Java 对象实例。它不是普通的 `new` 出来的对象，而是容器创建、装配和管理的。 | 承载应用的业务逻辑、配置、数据访问等功能。每个 Bean 是一个可被注入和复用的组件。 | 将"对象创建"和"对象使用"分离。开发者只需声明依赖关系，容器负责装配和生命周期管理，实现了控制反转（IoC）。 |
| **自动配置 (Auto-Configuration)** | Spring Boot 根据 classpath 中的 jar 包、已有 Bean、配置属性，自动推断并注册合适的 Bean 的机制。 | 消除手动编写 `@Configuration` 类的工作量——引入 `spring-boot-starter-web` 后无需手动配置 `DispatcherServlet`、`ViewResolver`、`HttpMessageConverter` 等。 | 解决"每个项目都要重复写配置"的问题。Spring Boot 的口号"约定优于配置"（Convention over Configuration）的核心实现。 |
| **Starter** | 一组预定义的 Maven 依赖集合 + 配套的自动配置类。命名遵循 `spring-boot-starter-{name}`（官方）或 `{name}-spring-boot-starter`（第三方）。 | 一行依赖解决过去需要加 5-10 个依赖的问题。例如 `spring-boot-starter-web` 一次性引入 Spring MVC、内嵌 Tomcat、Jackson JSON 等。 | 避免"依赖地狱"（版本冲突、遗漏依赖）。Starter 统一管理依赖版本（由 Spring Boot 的 BOM 控制），确保兼容性。 |
| **`@Conditional` 条件装配** | 在满足特定条件时才注册 Bean 的注解系列（`@ConditionalOnClass`、`@ConditionalOnMissingBean`、`@ConditionalOnProperty` 等）。 | 实现"有则启用，无则忽略"的智能注册。例如 classpath 中有 `DataSource` 时才自动配置数据源。 | 自动配置的核心机制——不同项目引入的依赖不同，条件装配确保只创建当前环境需要的 Bean，避免启动报错或无用 Bean 占用资源。 |
| **`@ConfigurationProperties`** | 将 `application.yml` 中的配置属性自动绑定到 Java 对象的注解。支持类型安全的访问路径（如 `app.greeting.message`）。 | 替代 `@Value` 的批量、结构化配置方案。一个 POJO 字段对应一个 YAML 属性，IDE 可自动补全。 | 配置管理的"类型安全化"。避免散落各处的 `@Value` 字符串注入，将配置集中管理、可校验、可导航。 |
| **Profile（多环境）** | 一组命名的配置环境（如 `dev`、`test`、`prod`），同一份代码在不同 Profile 下可注册不同的 Bean、加载不同的配置文件。 | 实现"开发环境连本地数据库，生产环境连集群"的切换，无需改代码。 | 12-Factor App 的"配置与代码分离"原则。环境差异通过 Profile 管理，而非 if-else 硬编码或注释切换。 |

## 3. 核心概念 / Core Concepts

### 2.1 @SpringBootApplication 拆解

```
@SpringBootApplication
    │
    ├── @SpringBootConfiguration    ← 本质是 @Configuration
    │
    ├── @EnableAutoConfiguration    ← 触发自动配置
    │       │
    │       └── @Import(AutoConfigurationImportSelector.class)
    │               │
    │               └── 读取 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
    │                   (Boot 2.x 用 spring.factories)
    │
    └── @ComponentScan              ← 扫描当前包及子包
```

### 2.2 自动配置加载流程

```
应用启动
    │
    ▼
AutoConfigurationImportSelector
    │
    ▼
加载所有 AutoConfiguration.imports 中声明的配置类
    │
    ▼
@Conditional 条件过滤
    │
    ├── @ConditionalOnClass → classpath 有对应类？
    ├── @ConditionalOnMissingBean → 用户没自定义？
    ├── @ConditionalOnProperty → 配置开关打开？
    │
    ▼
满足条件的配置类生效，注册 Bean
    │
    ▼
用户自定义 Bean 优先（@ConditionalOnMissingBean 保证）
```

### 2.3 Starter 命名规范

| 类型 | 命名格式 | 示例 |
|------|---------|------|
| 官方 Starter | `spring-boot-starter-{name}` | `spring-boot-starter-web` |
| 第三方 Starter | `{name}-spring-boot-starter` | `mybatis-spring-boot-starter` |

### 2.4 自定义 Starter 结构

```
my-spring-boot-starter/
├── my-spring-boot-autoconfigure/          ← 自动配置模块
│   ├── src/main/java/
│   │   └── com/example/autoconfigure/
│   │       ├── MyAutoConfiguration.java   ← @AutoConfiguration + @Conditional
│   │       └── MyProperties.java          ← @ConfigurationProperties
│   └── src/main/resources/
│       └── META-INF/spring/
│           └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│
└── my-spring-boot-starter/                ← Starter 模块（仅依赖聚合）
    └── pom.xml                            ← 依赖 autoconfigure + 业务库
```

### 2.5 @ConfigurationProperties 配置绑定

| 特性 | 说明 |
|------|------|
| 类型安全 | 配置值绑定到 Java 对象，IDE 自动补全 |
| 松散绑定 | `my-prop` = `myProp` = `MY_PROP` |
| 校验支持 | 配合 `@Validated` + JSR 303 注解 |
| 嵌套对象 | 支持 Map、List、嵌套 POJO |
| 不可变绑定 | 支持构造器绑定和 Java Record |
| 元数据生成 | `spring-boot-configuration-processor` 生成 IDE 提示 |

### 2.6 Profile 多环境管理

| 机制 | 说明 |
|------|------|
| `application-{profile}.yml` | 按 Profile 加载不同配置文件 |
| `spring.profiles.active` | 激活 Profile（命令行 / 环境变量 / 配置文件） |
| `spring.profiles.group` | Profile 分组，一次激活多个 |
| `@Profile("dev")` | Bean 仅在指定 Profile 下注册 |

**配置文件加载优先级（从高到低）：**

```
命令行参数 --server.port=9090
    │
    ▼
环境变量 SERVER_PORT=9090
    │
    ▼
application-{profile}.yml
    │
    ▼
application.yml
    │
    ▼
@PropertySource 指定的文件
    │
    ▼
默认值 (@Value 的 default)
```

### 2.7 打包与部署

| 方式 | 命令 | 产物 | 适用场景 |
|------|------|------|---------|
| Fat JAR | `mvn package` | 可执行 JAR（内嵌 Tomcat） | 主流方式 |
| WAR | 修改 packaging + 继承 `SpringBootServletInitializer` | WAR 包 | 部署到外部 Tomcat |
| Docker | `mvn spring-boot:build-image` | OCI 镜像 | 容器化部署 |
| GraalVM Native | `mvn -Pnative native:compile` | 原生可执行文件 | 极致启动速度 |

## 4. 快速集成 / Quick Start

### 3.1 Maven 依赖

| 依赖 | 说明 |
|------|------|
| `spring-boot-starter` | 核心 Starter（日志 + 自动配置 + YAML 支持） |
| `spring-boot-configuration-processor` | 生成配置元数据（IDE 提示），scope=`optional` |
| `spring-boot-starter-test` | 测试 Starter |

### 3.2 关键配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.profiles.active` | — | 激活的 Profile |
| `spring.profiles.group.*` | — | Profile 分组 |
| `spring.config.import` | — | 导入外部配置（文件/ConfigServer/Vault） |
| `spring.main.banner-mode` | `console` | Banner 显示模式 |
| `debug=true` | `false` | 打印自动配置报告 |

## 5. 设计决策与实现原理 / Design Decisions

> 以下结合 [`examples/spring-autoconfig-demo/`](../../examples/spring-autoconfig-demo/) 的实际代码，解释每个设计选择背后的"为什么"。

### 4.1 为什么 Demo 项目没有 `META-INF/spring/...AutoConfiguration.imports` 文件？

真正的 Spring Boot Starter 通过该文件声明自动配置类，由 `AutoConfigurationImportSelector` 加载。但 Demo 中 `CustomAutoConfiguration` 被 `@ComponentScan`（含在 `@SpringBootApplication` 中）扫描到，因此同样生效。

**这是教学简化**——省去了创建 `META-INF/spring/` 目录和 imports 文件的步骤，让学习者聚焦于 `@ConditionalOn*` 注解本身。在正式 Starter 开发中，必须通过 imports 文件注册。

### 4.2 为什么用 `@ConfigurationPropertiesScan` 而不是在每个 `@Configuration` 上加 `@EnableConfigurationProperties`？

```java
@SpringBootApplication
@ConfigurationPropertiesScan   // ← 一次性全局扫描
public class SpringAutoconfigDemoApplication { ... }
```

- **减少样板代码**：一个注解替代多处 `@EnableConfigurationProperties(AppProperties.class)`
- **约定优于配置**：自动发现包路径下的所有 `@ConfigurationProperties` 类
- **适合多模块项目**：新加配置类时无需修改已有配置类

### 4.3 为什么 `AppProperties` 使用嵌套静态类 + 预初始化？

```java
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final Greeting greeting = new Greeting();   // ← 预初始化，保证非 null
    private final Feature feature = new Feature();
    private final Cache cache = new Cache();
}
```

- **嵌套类镜像 YAML 层级**：`app.greeting.message` 对应 `AppProperties.Greeting.message`，IDE 自动补全可直接导航
- **预初始化防止 NPE**：即使用户未配置任何 `app.*`，`greeting`、`feature`、`cache` 也不为 null，内层默认值生效
- **`final` 确保引用不可变**：嵌套对象引用不会被意外替换

### 4.4 为什么 `@ConfigurationProperties` 要加 `@Validated`？

```java
@Data
@Validated   // ← 启用 Jakarta Validation
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    @NotBlank(message = "问候语消息不能为空")
    private String message = "Hello";
}
```

- **启动时校验**：如果 YAML 中 `app.greeting.message:` 设为空字符串，应用启动立即失败（fail-fast），而非运行时才发现
- **配合 `configuration-processor`**：`spring-boot-configuration-processor` 读取 `@NotBlank` 等注解生成 `spring-configuration-metadata.json`，IDE 可提示校验规则

### 4.5 为什么 `FeatureService` 用 `@EventListener(ApplicationReadyEvent.class)` 而非 `@PostConstruct`？

```java
@EventListener(ApplicationReadyEvent.class)
public void onApplicationReady() {
    System.out.println("应用已就绪，功能状态: " + feature.isEnabled());
}
```

| 对比 | `@PostConstruct` | `@EventListener(ApplicationReadyEvent)` |
|------|------------------|----------------------------------------|
| 触发时机 | Bean 初始化完成后 | **所有** Bean 初始化完成 + 容器就绪 |
| 依赖安全 | 依赖的 Bean 可能还未就绪 | 所有 Bean 已就绪，包括自动配置的 Bean |
| 适用场景 | Bean 自身资源初始化 | 依赖全局状态的启动后检查 |

Demo 中需要在应用完全就绪后报告功能状态，因此选择 `ApplicationReadyEvent`。

### 4.6 为什么 `GreetingService` 是纯 POJO（没有 `@Service` 注解）？

```java
// GreetingService.java — 没有任何 Spring 注解
public class GreetingService {
    public GreetingService(String message) { this.message = message; }
}
```

**刻意为之**——展示 `@ConditionalOnMissingBean` 的核心机制：
- Bean 由 `CustomAutoConfiguration.defaultGreetingService()` 方法注册
- 用户可以自行创建带有 `@Service` 的 `GreetingService` 子类覆盖默认实现
- 如果 `GreetingService` 自带 `@Service`，则 `@ConditionalOnMissingBean` 检测到已有 Bean，默认实现不会注册

### 4.7 为什么同一 Controller 中同时使用 `@Value` 和 `@ConfigurationProperties`？

```java
@Value("${spring.application.name:unknown}")   // 简单值，宽松匹配
private String applicationName;

private final AppProperties appProperties;     // 结构化配置，类型安全
```

| 特性 | `@Value` | `@ConfigurationProperties` |
|------|---------|--------------------------|
| 适用场景 | 单个简单属性 | 一组相关属性 |
| 类型安全 | 手动转换 | 自动绑定 |
| IDE 提示 | 无 | 有（配合 processor） |
| 校验支持 | 无 | `@Validated` 支持 |
| 松散绑定 | 不支持 | 支持 |

两种方式在同一 Controller 中共存，是为了**对比教学**：让学习者直观感受"单个值用 `@Value`，结构化配置用 `@ConfigurationProperties`"的选择依据。

### 4.8 为什么 `@ConditionalOnProperty` 的 `matchIfMissing` 在 feature 和 cache 上不同？

```java
// feature 功能：默认关闭 → matchIfMissing = false（缺少配置时不注册）
@ConditionalOnProperty(name = "app.feature.enabled", havingValue = "true", matchIfMissing = false)

// cache 功能：默认开启 → matchIfMissing = true（缺少配置时仍然注册）
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true", matchIfMissing = true)
```

- **Feature（业务功能开关）**：保守策略——默认关闭，用户显式 `true` 才启用，防止未预期的功能泄露到生产环境
- **Cache（性能优化）**：激进策略——默认开启，提供开箱即用的缓存加速，用户有特殊需求时可通过配置关闭

这是 Spring Boot 自动配置中的常见模式：`matchIfMissing` 的值取决于该功能"默认开"还是"默认关"对用户更友好。

### 4.9 为什么 `application.yml` 中开启 `debug` 级日志到 `org.springframework.boot.autoconfigure`？

```yaml
logging:
  level:
    org.springframework.boot.autoconfigure: DEBUG
```

启动时会输出**自动配置报告**（Positive/Negative matches），清晰展示每个自动配置类是否生效及其原因。这是排查"为什么某个 Bean 没有被自动注册"的最有效手段。

## 6. 进阶要点 / Advanced Topics

- **自动配置排除** — `@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})` 或 `spring.autoconfigure.exclude` 配置
- **自动配置报告** — 启动时加 `--debug` 或设置 `debug=true`，查看 Positive/Negative matches
- **`@AutoConfiguration` 排序** — `before` / `after` 属性控制自动配置类的加载顺序
- **配置加密** — Jasypt (`jasypt-spring-boot-starter`) 加密敏感配置
- **外部化配置** — `spring.config.import` 支持从 ConfigServer、Vault、Consul 加载配置
- **Layered JAR** — `spring-boot-maven-plugin` 分层打包，优化 Docker 镜像构建缓存
- **AOT 处理** — Spring Boot 的 Ahead-of-Time 编译，为 GraalVM Native Image 做准备
- **虚拟线程** — Spring Boot 4.0 默认在 Java 21+ 环境下启用虚拟线程，Tomcat 自动使用虚拟线程执行器
- **Jackson 3 支持** — Spring Boot 4.0 引入 Jackson 3（`tools.jackson`），注意包名变化

## 7. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 自动配置不生效 | 缺少依赖或条件不满足 | 用 `--debug` 查看自动配置报告 |
| 配置属性不生效 | Profile 未激活或优先级被覆盖 | 检查 `spring.profiles.active` 和配置加载顺序 |
| `@ConfigurationProperties` 无法绑定 | 缺少 `@EnableConfigurationProperties` 或 `@ConfigurationPropertiesScan` | 确认注册方式 |
| 启动慢 | Bean 初始化耗时 | 开启 `spring.main.lazy-initialization` 或用 startup actuator 分析 |
| JAR 包太大 | 包含所有依赖 | 使用 Layered JAR + Docker 多阶段构建 |
| 多模块项目扫描不到 Bean | 主类不在根包 | 调整包结构或显式 `@ComponentScan` |

## 8. 示例项目 / Example

> 示例项目位于 [`examples/spring-autoconfig-demo/`](../../examples/spring-autoconfig-demo/)
>
> 已演示：`@ConfigurationProperties` 类型安全绑定、嵌套配置类、`@Validated` 配置校验、`@ConditionalOnClass` / `@ConditionalOnMissingBean` / `@ConditionalOnProperty` 条件装配、`@ConfigurationPropertiesScan` 全局扫描、`ApplicationReadyEvent` 启动事件、多环境 Profile 切换（dev / test / prod）、`@Value` vs `@ConfigurationProperties` 对比

## 9. 参考链接 / References

- [Spring Boot Reference — Auto-configuration](https://docs.spring.io/spring-boot/reference/using/auto-configuration.html)
- [Spring Boot Reference — Configuration Properties](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [Spring Boot Reference — Creating Your Own Starter](https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html)
- [Baeldung — Create a Custom Starter](https://www.baeldung.com/spring-boot-custom-starter)

## 10. 下一步

理解了自动配置原理之后，核心基础篇的最后一站是事务管理 — 掌握 `@Transactional` 的传播行为、隔离级别，以及生产环境中最常见的事务失效场景。

→ [事务管理](04-transaction.md)
