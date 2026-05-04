# Spring 核心基础篇

> Spring Framework 和 Spring Boot 的核心机制，是学习整个 Spring 生态的根基。

## 版本基准

| 组件 | 版本 |
|------|------|
| Java | 21 (LTS) |
| Spring Framework | 7.0.6 |
| Spring Boot | 4.0.5 |

## 为什么从这里开始

Spring Boot 的强大建立在 Spring Framework 的核心基础之上。跳过 IoC、MVC、自动配置等核心概念直接使用 Spring Boot，会在遇到问题时难以定位根因。本篇的目标是打牢基础。

每个子主题深度足够独立成章，拆分后可按需学习、维护清晰。

## 文档索引

| 文档 | 主题 | 关键词 |
|------|------|--------|
| [IoC 与依赖注入](01-ioc-di.md) | IoC 容器、Bean 生命周期、作用域、条件装配 | `@Component` `@Autowired` `@Conditional` `BeanPostProcessor` |
| [Spring MVC Web 开发](02-spring-mvc.md) | Controller、参数校验、统一异常处理、拦截器 | `@RestController` `@Valid` `@ControllerAdvice` `HandlerInterceptor` |
| [自动配置与 Starter](03-auto-configuration.md) | 自动配置原理、自定义 Starter、Profile、打包部署 | `@SpringBootApplication` `@ConfigurationProperties` `spring.profiles` |
| [事务管理](04-transaction.md) | 声明式事务、传播行为、隔离级别、事务失效场景 | `@Transactional` `Propagation` `Isolation` `PlatformTransactionManager` |

## 学习顺序

```
IoC & DI ──> Spring MVC ──> 自动配置与 Starter ──> 事务管理
  (1)          (2)              (3)                  (4)
```

1. 先理解 IoC 容器和 Bean 管理，这是 Spring 一切功能的基石
2. 掌握 Web 开发基础，能写出标准的 RESTful API
3. 理解 Spring Boot 的自动配置魔法，学会自定义和调优
4. 深入事务管理，避免生产环境中常见的事务陷阱

## 示例项目

| 示例项目 | 对应文档 | 状态 |
|---------|---------|------|
| `examples/spring-ioc-demo/` | IoC 与依赖注入 | ✅ 已创建 |
| `examples/spring-mvc-demo/` | Spring MVC | ✅ 已创建 |
| `examples/spring-autoconfig-demo/` | 自动配置与 Starter | ✅ 已创建 |
| `examples/spring-transaction-demo/` | 事务管理 | ✅ 已创建 |

## 与其他篇章的关系

```
                    ┌─────────────────────────────────┐
                    │     核心基础篇（本篇）              │
                    │  IoC / MVC / AutoConfig / Tx     │
                    └──────────┬──────────────────────┘
                               │
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
     ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
     │   数据库篇    │ │  框架核心篇   │ │   微服务篇    │
     └──────────────┘ └──────────────┘ └──────────────┘
                               │
                               ▼
                      ┌──────────────┐
                      │   进阶主题篇  │
                      └──────────────┘
```

核心基础篇是所有其他篇章的依赖基座，无内部依赖。

## 注意事项

- IoC 容器的 Bean 作用域和生命周期在 Web 环境下与单元测试中行为不同，示例需覆盖两种场景
- 事务失效是 Spring 高频踩坑点，文档覆盖了所有常见失效场景（自调用、非 public 方法、异常类型不匹配等）
- 自动配置的 `@ConditionalOn*` 注解优先级和互斥关系是排查启动问题的关键
- 核心基础篇示例项目使用 H2 内存数据库，确保零外部依赖即可运行
