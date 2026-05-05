---
title: Java Spring AOP
created: 2026-05-04 22:30:00
category: Java-Spring
tags:
  - Java
  - Spring
  - AOP
  - 切面编程
---

<!-- markdownlint-disable MD025 -->

# Java Spring AOP（面向切面编程）

## 为什么要学 AOP

前面 IoC、Bean、DI 解决了"对象的创建和依赖管理"问题。但还有一类问题它们解决不了：**横切关注点（Cross-Cutting Concerns）**。

什么是横切关注点？日志记录、事务管理、权限检查、性能监控、缓存处理 -- 这些功能散落在数十上百个方法中，跟具体的业务逻辑无关，但每个方法都需要。如果没有 AOP，你只能在每个方法里重复写这些代码。AOP 就是用来解决这个问题的。

---

## 核心概念

### AOP 是什么

**AOP（Aspect-Oriented Programming，面向切面编程）是一种编程范式，允许你把横切关注点（如日志、事务、安全）从业务逻辑中分离出来，以"切面"的形式统一管理。**

换个说法：AOP 让你可以定义"在某类方法执行前后，插入一段逻辑"，而不需要修改那些方法本身的代码。

Spring AOP 最典型的应用就是你每天都在用但可能没意识到的 -- `@Transactional`。你在 Service 方法上加了这个注解，Spring 就在方法执行前后自动开启事务、提交或回滚。这就是 AOP 在工作。

### 为什么需要 AOP

**核心原因：消除重复代码，保持业务逻辑纯净。**

假设一个系统有 100 个 API 接口，每个接口都需要记录请求日志。没有 AOP 的做法：

```java
@PostMapping("/users")
public User createUser(@RequestBody CreateUserRequest request) {
    log.info("Request: createUser, params: {}", request);
    // 实际业务逻辑...
    log.info("Response: {}", result);
    return result;
}
```

这段日志代码跟业务无关，但每个方法都得写。100 个方法就是 100 份重复。更糟的是，如果哪天要改日志格式或加新字段，就要改 100 个地方。

有了 AOP，只需要定义一次：

```java
@Aspect
@Component
public class LoggingAspect {
    @Around("execution(* com.example.controller.*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Enter: {}", joinPoint.getSignature());
        Object result = joinPoint.proceed();
        log.info("Exit: {}", result);
        return result;
    }
}
```

一行 `@Around` 注解，覆盖了所有 Controller 方法。改日志逻辑也只需要改这一个地方。

---

### 没有 AOP 会怎样

业务代码和基础设施代码（日志、事务、权限）混在一起，方法体臃肿，核心逻辑被淹没。修改日志格式要改遍所有方法。有了 AOP，业务方法保持纯净，横切逻辑集中在切面类中，改一处即可全局生效。Spring 的 `@Transactional` 就是 AOP 最经典的应用。

---

## 概念深入解释

### AOP 的核心术语

AOP 有一套自己的术语体系，用"拦截方法调用并记录日志"这个场景来映射：

**Aspect（切面）** -- 横切关注点的模块化。一个 `@Aspect` 标注的类，里面包含多个 Advice。就是一个"拦截器套件"，里面定义了"在哪些地方做什么事"。

**Pointcut（切入点）** -- 定义在哪些方法上生效的匹配表达式：

```java
// 匹配 UserService 类的所有方法
@Pointcut("execution(* com.example.service.UserService.*(..))")

// 匹配所有带 @GetMapping 注解的方法
@Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)")

// 匹配 controller 包下的所有方法
@Pointcut("within(com.example.controller..*)")
```

**Advice（通知）** -- 在切入点上执行什么逻辑：

| 类型 | 注解 | 执行时机 |
|------|------|----------|
| Before | `@Before` | 方法执行前 |
| AfterReturning | `@AfterReturning` | 方法正常返回后 |
| AfterThrowing | `@AfterThrowing` | 方法抛出异常后 |
| After | `@After` | 方法结束后（无论成功失败） |
| Around | `@Around` | 环绕 -- 前后都执行，可决定是否继续执行 |

`@Around` 最强大也最常用，它可以修改参数或返回值、选择不执行目标方法（缓存命中时直接返回）、吞掉异常或转换异常。

**Join Point（连接点）** -- 程序执行过程中的某个时刻。在 Spring AOP 中，连接点几乎总是方法调用。

**Weaving（织入）** -- 把切面应用到目标对象上的过程。Spring AOP 在运行时通过动态代理织入。

### Spring AOP 的底层原理：动态代理

当你给一个类加了 AOP 切面，Spring 不会直接把这个类的实例给你，而是生成一个代理对象包装它。这个代理对象看起来跟原始对象一样，但实际上每个方法调用都先经过 Advice。

Spring 提供了两种代理方式：

- **JDK 动态代理** -- 目标类必须实现接口。适用于 Service 接口 + ServiceImpl 的模式。
- **CGLIB 代理** -- 通过继承目标类生成代理。适用于没有接口的类；Spring Boot 默认 AOP 配置通常使用这种代理方式。

这意味着一个关键限制：**Spring AOP 只能拦截经过 Spring 代理对象发起的方法调用。** 实际项目中应优先把切面应用在 Spring 管理的 Bean 的 public 方法上；private 方法、final 方法、非 Bean 对象的方法调用、同一个类内部的 `this.xxx()` 调用都不会按预期经过代理。

### AOP 的实际应用场景

| 场景 | 说明 |
|------|------|
| **事务管理** | `@Transactional` -- Spring 用 AOP 在方法前后管理事务 |
| **安全控制** | `@PreAuthorize` -- 方法执行前检查权限 |
| **缓存** | `@Cacheable` -- 方法执行前检查缓存，执行后写入缓存 |
| **日志** | 统一的请求日志、操作审计 |
| **性能监控** | 记录方法执行时间 |
| **重试** | 方法执行失败时自动重试 |

### 内部调用陷阱

同一个类内部的方法调用不会走代理：

```java
@Service
public class UserService {
    @Transactional
    public void createUser(UserDTO dto) { /* 事务生效 */ }

    public void batchCreate(List<UserDTO> dtos) {
        for (UserDTO dto : dtos) {
            this.createUser(dto); // 事务不生效！直接调用不走代理
        }
    }
}
```

解决方法：把被调用的方法放到另一个 Bean 中，或者通过 `@Autowired` 注入自身并通过注入的代理调用。

### 代理对象与原始对象的区别

如果你通过 `getClass()` 打印注入的 Bean 的类型，你会发现它不是你的原始类名，而是一个类似 `UserService$$EnhancerBySpringCGLIB` 的代理类名。这是正常现象，说明 AOP 在生效。

### 性能影响

每次 AOP 调用都比直接调用多一层代理开销。对 99% 的业务场景，这个开销可以忽略不计。但在高并发热点路径（每秒数万次调用的方法）上，需要注意 AOP 切面中的逻辑是否高效。

---

## 核心要点

1. **AOP 解决横切关注点** -- 把散落在各处的相同逻辑收归一处管理。
2. **Aspect = 切面，Pointcut = 在哪里生效，Advice = 做什么事。**
3. **Spring AOP 底层是动态代理** -- 你拿到的 Bean 是代理对象，不是原始对象。
4. **@Transactional 就是 AOP** -- 理解 AOP 才能理解事务注解为什么能自动管理事务。
5. **内部调用不走代理是常见坑。** 需要拆 Bean 或用注入自身的方式解决。

---

## 常见误区

- **以为 AOP 可以拦截任何方法调用。** Spring AOP 是基于代理的，只拦截经过代理对象的调用。private 方法、final 方法、非 Bean 对象、同类内部 `this.xxx()` 调用都不在正常拦截路径内。
- **在切面中抛出检查型异常导致代理异常。** Advice 方法如果抛出与目标方法签名不兼容的异常，会导致 `UndeclaredThrowableException`。Around Advice 最好包装或转换异常。
- **依赖 AOP 的执行顺序。** 当多个切面作用在同一个方法上时，执行顺序是不确定的（除非显式用 `@Order` 注解排序）。不要依赖隐式的执行顺序。
- **忘记启动 @EnableAspectJAutoProxy。** 在 Spring Boot 中自动配置已经帮你开启了，但如果你用纯 Spring 或自定义配置，需要手动加这个注解。

---

## 与其他概念的关联

- **前置：** [Java Spring Bean](./06_Java%20Spring%20Bean.md) -- AOP 作用在 Bean 上。你拿到的是代理 Bean，不是原始对象。理解 Bean 的生命周期后才能理解 AOP 在哪个阶段织入（初始化阶段之后）。[Java Spring DI](./07_Java%20Spring%20DI.md) -- DI 注入的其实也是代理 Bean（如果该类有 AOP 切面的话）。[Java 反射基础](./03_Java%20反射基础.md) -- JDK 动态代理依赖反射来调用目标方法。
- **并行：** [Java Spring 容器](./09_Java%20Spring%20容器.md) -- 容器负责在 Bean 初始化阶段创建 AOP 代理对象。
- **后续：** [Java Spring 事务管理](Java%20Spring%20事务管理.md) -- `@Transactional` 是 AOP 最经典的应用，理解 AOP 后理解事务管理会水到渠成。[Java Spring Security](Java%20Spring%20Security.md) -- Spring Security 的方法级安全（`@PreAuthorize`、`@Secured`）也基于 AOP。
