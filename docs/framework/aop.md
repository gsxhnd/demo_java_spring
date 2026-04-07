# Spring AOP — 面向切面编程 / Aspect-Oriented Programming

> 横切关注点：日志、权限、事务、性能监控的统一处理

## 1. 概述 / Overview

AOP（Aspect-Oriented Programming）将散布在多个类中的横切关注点（Cross-Cutting Concerns）抽取到独立的切面中，避免代码重复。

### 没有 AOP vs 有 AOP

```
没有 AOP：                        有 AOP：

UserService.create() {            UserService.create() {
  log.info("开始创建用户");           // 只关注业务逻辑
  checkPermission();                createUser(user);
  long start = now();             }
  try {
    createUser(user);             @Aspect LogAspect     → 自动记录日志
  } finally {                     @Aspect AuthAspect    → 自动检查权限
    log.info("耗时:" + cost);      @Aspect MetricsAspect → 自动统计耗时
  }                               @Transactional        → 自动管理事务
}
```

### 常见应用场景

| 场景 | 说明 |
|---|---|
| 日志记录 | 方法入参、出参、耗时自动记录 |
| 权限校验 | 自定义注解 `@RequirePermission` |
| 事务管理 | `@Transactional`（Spring 内置 AOP 实现） |
| 性能监控 | 方法执行耗时统计 |
| 缓存 | `@Cacheable`（Spring Cache 内置 AOP 实现） |
| 异常处理 | 统一异常捕获和转换 |
| 参数校验 | 自定义校验注解 |
| 限流 | 自定义 `@RateLimit` 注解 |

---

## 2. 核心概念 / Core Concepts

### AOP 术语

| 术语 | 说明 |
|---|---|
| **Aspect（切面）** | 横切关注点的模块化（一个类，用 `@Aspect` 标记） |
| **Join Point（连接点）** | 程序执行的某个点（Spring AOP 中只支持方法执行） |
| **Pointcut（切点）** | 匹配 Join Point 的表达式（定义"在哪里"切入） |
| **Advice（通知）** | 切面在切点处执行的动作（定义"做什么"） |
| **Target（目标对象）** | 被代理的原始对象 |
| **Proxy（代理）** | AOP 创建的代理对象 |

### Advice 类型

| 类型 | 注解 | 执行时机 |
|---|---|---|
| Before | `@Before` | 方法执行前 |
| After Returning | `@AfterReturning` | 方法正常返回后 |
| After Throwing | `@AfterThrowing` | 方法抛出异常后 |
| After | `@After` | 方法执行后（无论成功失败） |
| **Around** | `@Around` | 包围方法执行（最强大，可控制是否执行） |

### Pointcut 表达式

| 表达式 | 说明 |
|---|---|
| `execution(* com.example.service.*.*(..))` | 匹配 service 包下所有方法 |
| `execution(public * *(..))` | 匹配所有 public 方法 |
| `@annotation(com.example.anno.Log)` | 匹配带 `@Log` 注解的方法 |
| `@within(org.springframework.stereotype.Service)` | 匹配 `@Service` 类中的所有方法 |
| `bean(userService)` | 匹配名为 userService 的 Bean |

### Spring AOP vs AspectJ

| 特性 | Spring AOP | AspectJ |
|---|---|---|
| 实现方式 | 动态代理（JDK / CGLIB） | 编译时/加载时织入 |
| 连接点 | 仅方法执行 | 方法、构造器、字段访问等 |
| 性能 | 运行时代理，略有开销 | 编译时织入，无运行时开销 |
| 使用难度 | 简单（注解配置） | 复杂（需要 AspectJ 编译器） |
| 推荐 | 绝大多数场景够用 | 极端性能要求 |

---

## 3. 快速集成 / Quick Start

### Maven 依赖

- `spring-boot-starter-aop` — 自动引入 AspectJ Weaver

### 关键注解

| 注解 | 说明 |
|---|---|
| `@Aspect` | 声明切面类 |
| `@Component` | 切面类需注册为 Spring Bean |
| `@Pointcut` | 定义可复用的切点表达式 |
| `@Before` / `@After` / `@Around` | 通知类型 |
| `@Order(n)` | 多个切面的执行顺序（数字越小越先执行） |

---

## 4. 进阶要点 / Advanced Topics

- **自定义注解 + AOP**：定义 `@Log`、`@RateLimit`、`@RequirePermission` 等注解，用 `@annotation()` 切点匹配
- **`@Around` 环绕通知**：最常用，可以控制目标方法是否执行、修改入参和返回值
- **`ProceedingJoinPoint`**：Around 通知中获取方法签名、参数、执行目标方法
- **多切面排序**：`@Order` 控制执行顺序，Before 按 Order 升序，After 按 Order 降序
- **AOP 失效场景**：同类内部方法调用不走代理（`this.method()` 不触发 AOP），需通过 `AopContext.currentProxy()` 或注入自身解决
- **CGLIB vs JDK 动态代理**：Spring Boot 默认使用 CGLIB（代理类），JDK 代理要求目标实现接口

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| AOP 不生效 | 确认切面类加了 `@Aspect` + `@Component` |
| 同类方法调用不触发 AOP | 内部调用不走代理，用 `AopContext.currentProxy()` |
| `@Transactional` 不生效 | 同上，事务也是 AOP 实现，内部调用不触发 |
| 切点表达式匹配不到 | 检查包路径、方法签名是否正确 |
| 多切面执行顺序不对 | 用 `@Order` 显式指定顺序 |

---

## 6. 示例项目 / Example

AOP 示例集成在各示例项目中（日志切面、性能监控切面）。

## 7. 参考链接 / References

- [Spring AOP 官方文档](https://docs.spring.io/spring-framework/reference/core/aop.html)
- [AspectJ 表达式参考](https://www.eclipse.org/aspectj/doc/released/progguide/index.html)
