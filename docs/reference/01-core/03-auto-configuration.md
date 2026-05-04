# Spring Boot 自动配置与 Starter 机制 / Auto-Configuration & Starter

> Spring Boot 的"约定优于配置"魔法背后，是一套精密的自动配置机制。理解它，才能在需要时精准调优和自定义。

## 1. 概述 / Overview

Spring Boot 通过自动配置（Auto-Configuration）根据 classpath 中的依赖、已有 Bean 和配置属性，自动注册合适的 Bean。Starter 则是一组预定义的依赖集合 + 自动配置，实现"引入依赖即可用"的开发体验。

## 2. 核心概念 / Core Concepts

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

## 3. 快速集成 / Quick Start

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

## 4. 进阶要点 / Advanced Topics

- **自动配置排除** — `@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})` 或 `spring.autoconfigure.exclude` 配置
- **自动配置报告** — 启动时加 `--debug` 或设置 `debug=true`，查看 Positive/Negative matches
- **`@AutoConfiguration` 排序** — `before` / `after` 属性控制自动配置类的加载顺序
- **配置加密** — Jasypt (`jasypt-spring-boot-starter`) 加密敏感配置
- **外部化配置** — `spring.config.import` 支持从 ConfigServer、Vault、Consul 加载配置
- **Layered JAR** — `spring-boot-maven-plugin` 分层打包，优化 Docker 镜像构建缓存
- **AOT 处理** — Spring Boot 的 Ahead-of-Time 编译，为 GraalVM Native Image 做准备
- **虚拟线程** — Spring Boot 4.0 默认在 Java 21+ 环境下启用虚拟线程，Tomcat 自动使用虚拟线程执行器
- **Jackson 3 支持** — Spring Boot 4.0 引入 Jackson 3（`tools.jackson`），注意包名变化

## 5. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 自动配置不生效 | 缺少依赖或条件不满足 | 用 `--debug` 查看自动配置报告 |
| 配置属性不生效 | Profile 未激活或优先级被覆盖 | 检查 `spring.profiles.active` 和配置加载顺序 |
| `@ConfigurationProperties` 无法绑定 | 缺少 `@EnableConfigurationProperties` 或 `@ConfigurationPropertiesScan` | 确认注册方式 |
| 启动慢 | Bean 初始化耗时 | 开启 `spring.main.lazy-initialization` 或用 startup actuator 分析 |
| JAR 包太大 | 包含所有依赖 | 使用 Layered JAR + Docker 多阶段构建 |
| 多模块项目扫描不到 Bean | 主类不在根包 | 调整包结构或显式 `@ComponentScan` |

## 6. 示例项目 / Example

> 示例项目位于 [`examples/spring-boot-starter-demo/`](../../examples/spring-boot-starter-demo/)（待创建）
>
> 将演示：自定义 Starter、@ConfigurationProperties、Profile 切换、自动配置排除

## 7. 参考链接 / References

- [Spring Boot Reference — Auto-configuration](https://docs.spring.io/spring-boot/reference/using/auto-configuration.html)
- [Spring Boot Reference — Configuration Properties](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [Spring Boot Reference — Creating Your Own Starter](https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html)
- [Baeldung — Create a Custom Starter](https://www.baeldung.com/spring-boot-custom-starter)

## 8. 下一步

理解了自动配置原理之后，核心基础篇的最后一站是事务管理 — 掌握 `@Transactional` 的传播行为、隔离级别，以及生产环境中最常见的事务失效场景。

→ [事务管理](04-transaction.md)
