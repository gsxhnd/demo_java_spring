# 核心基础模块 (core)

> Spring 框架核心概念：IoC 容器、MVC 模式、自动配置原理、声明式事务

## 设计决策

### 为什么需要这个模块

Spring Boot 的强大建立在 Spring Framework 的核心基础之上。开发者如果跳过 IoC、MVC、自动配置等核心概念直接使用 Spring Boot，会在遇到问题时难以定位根因。本模块的目标是打牢基础。

### 为什么这么设计

- **选择了**：将 IoC、MVC、AutoConfig、Transaction 四个子主题拆分为独立文档
- **而不是**：将它们合并为一个"Spring Boot 基础"文档
- **原因**：每个子主题深度足够独立成章，拆分后可按需学习、维护清晰

## 关键类型与接口

### 示例项目架构

每个示例项目遵循统一的包结构：

- **定义位置**：`examples/spring-{topic}-demo/src/main/java/com/example/{topic}/`
- **用途**：演示核心基础概念
- **使用场景**：`spring-ioc-demo`, `spring-mvc-demo`, `spring-autoconfig-demo`, `spring-transaction-demo`

## 模块结构

```text
docs/reference/core/
├── README.md              # 核心基础篇索引
├── ioc-di.md              # IoC 容器与依赖注入
├── spring-mvc.md          # Spring MVC Web 开发
├── auto-configuration.md  # 自动配置与 Starter
└── transaction.md         # 声明式事务管理

examples/
├── spring-ioc-demo/       # IoC 示例
├── spring-mvc-demo/       # MVC 示例
├── spring-autoconfig-demo/# 自动配置示例
└── spring-transaction-demo/# 事务示例
```

| 文件 | 职责 |
|------|------|
| `ioc-di.md` | ApplicationContext, @Component, @Autowired, Bean 生命周期 |
| `spring-mvc.md` | @RestController, @Valid, @ControllerAdvice, Interceptor |
| `auto-configuration.md` | @SpringBootApplication, @ConfigurationProperties, Profile |
| `transaction.md` | @Transactional, Propagation, Isolation, 事务失效场景 |

## 与其他模块的关系

### 依赖

- 无内部依赖（基础层）

### 被依赖

- **数据库模块**：数据库集成基于 IOC/MVC 基础
- **框架核心模块**：Security、AOP 等基于 MVC 基础
- **微服务模块**：微服务框架基于 Spring Boot 核心机制

### 依赖关系图

```text
核心基础 (core)
  ↑ 依赖基座
  ├── 数据库 (database)
  ├── 框架核心 (framework)
  ├── 微服务 (microservice)
  └── 进阶主题 (advanced)
```

## 详细技术参考

以下为各子主题的完整技术参考文档，包含核心概念、配置示例、代码片段、进阶要点和常见问题：

| 子主题 | 参考文档 |
|--------|----------|
| IoC 容器与依赖注入 | [reference/core/ioc-di.md](../reference/core/ioc-di.md) |
| Spring MVC Web 开发 | [reference/core/spring-mvc.md](../reference/core/spring-mvc.md) |
| 自动配置与 Starter | [reference/core/auto-configuration.md](../reference/core/auto-configuration.md) |
| 声明式事务管理 | [reference/core/transaction.md](../reference/core/transaction.md) |

## 注意事项

- IoC 容器的 Bean 作用域和生命周期在 Web 环境下与单元测试中行为不同，示例需覆盖两种场景
- 事务失效是 Spring 高频踩坑点，文档必须覆盖所有常见失效场景（自调用、非 public 方法、异常类型不匹配等）
- 自动配置的 `@ConditionalOn*` 注解优先级和互斥关系是排查启动问题的关键
- 示例项目使用 H2 内存数据库（事务示例），确保零外部依赖即可运行
