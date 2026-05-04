# Spring 核心基础 / Spring Core Fundamentals

> 本章涵盖 Spring Framework 和 Spring Boot 的核心机制，是学习整个 Spring 生态的根基。

## 版本基准 / Version Baseline

| 组件 | 版本 |
|------|------|
| Java | 21 (LTS) |
| Spring Framework | 7.0.6 |
| Spring Boot | 4.0.5 |

## 文档索引 / Document Index

| 文档 | 主题 | 关键词 |
|------|------|--------|
| [IoC 与依赖注入](ioc-di.md) | IoC 容器、Bean 生命周期、作用域、条件装配 | `@Component` `@Autowired` `@Conditional` `BeanPostProcessor` |
| [Spring MVC Web 开发](spring-mvc.md) | Controller、参数校验、统一异常处理、拦截器 | `@RestController` `@Valid` `@ControllerAdvice` `HandlerInterceptor` |
| [自动配置与 Starter](auto-configuration.md) | 自动配置原理、自定义 Starter、Profile、打包部署 | `@SpringBootApplication` `@ConfigurationProperties` `spring.profiles` |
| [事务管理](transaction.md) | 声明式事务、传播行为、隔离级别、事务失效场景 | `@Transactional` `Propagation` `Isolation` `PlatformTransactionManager` |

## 学习顺序 / Recommended Order

```
IoC & DI ──> Spring MVC ──> 自动配置与 Starter ──> 事务管理
  (1)          (2)              (3)                  (4)
```

1. 先理解 IoC 容器和 Bean 管理，这是 Spring 一切功能的基石
2. 掌握 Web 开发基础，能写出标准的 RESTful API
3. 理解 Spring Boot 的自动配置魔法，学会自定义和调优
4. 深入事务管理，避免生产环境中常见的事务陷阱

## 与其他章节的关系 / Relations

```
                    ┌─────────────────────────────────┐
                    │     Spring Core (本章)            │
                    │  IoC / MVC / AutoConfig / Tx     │
                    └──────────┬──────────────────────┘
                               │
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
     ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
     │   Database    │ │  Framework   │ │ Microservice │
     │  数据库集成    │ │  框架能力     │ │  微服务架构   │
     └──────────────┘ └──────────────┘ └──────────────┘
                               │
                               ▼
                      ┌──────────────┐
                      │   Advanced   │
                      │   进阶主题    │
                      └──────────────┘
```
