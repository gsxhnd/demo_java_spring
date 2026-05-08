---
title: Java Spring Security
created: 2026-05-08 22:29:19
category: Java-Spring
tags:
  - Java
  - Spring
  - Security
  - Authentication
  - Authorization
  - FilterChain
---

<!-- markdownlint-disable MD025 -->

# Java Spring Security

## 为什么要学 Spring Security

前面七个 Part 我们构建了一个功能完整的应用：有 API、有数据库、有业务逻辑、有缓存、有定时任务、有测试。但这个应用现在是**全裸的** — 任何人都可以访问所有接口，没有登录验证，没有权限控制。在真实项目中，安全不是可选项，而是基础设施。

你可能会想：手写一个登录拦截器不行吗？检查 Session 里有没用户信息，没有就重定向到登录页。这在简单场景下确实可行。但当需求变成"订单管理页只有管理员能看，普通用户只能看自己的订单"、"API 接口需要 JWT Token 验证"、"登录失败 3 次锁定账号"等复杂场景时，手写的安全代码会膨胀成难以维护的庞然大物。

Spring Security 是 Spring 生态中解决认证（Authentication）和授权（Authorization）问题的标准框架。它通过 Filter Chain 机制在请求到达 Controller 之前完成安全检查，让你以声明式的方式配置安全规则。

## 核心概念

### Spring Security 是什么

Spring Security 是一个基于 Servlet Filter 链的安全框架，负责在 HTTP 请求到达应用之前进行身份认证和权限检查。它的核心理念是：**安全逻辑不应该散落在业务代码中，而应该在请求入口处统一拦截处理**。

**换个说法：** 想象一栋大楼的安保系统。Spring Security 不是房间里装在各处的门锁，而是大楼入口的安检闸机 — 所有人进门时都必须验证身份（认证）和检查通行权限（授权）。一旦通过了闸机，在楼内（应用内部）就不再需要反复检查。

### 为什么需要 Spring Security

**痛点场景：** 一个应用需要实现这些安全功能：
- 某些 API 需要登录才能访问，某些不需要
- 管理员能访问 /admin/ 下的所有接口，普通用户只能访问 /user/ 下的接口
- 支持表单登录、JWT 无状态登录、OAuth2 第三方登录
- 防止 CSRF 攻击、防止 Session 固定攻击
- 密码需要加密存储

如果从零手写这些，安全代码量可能超过业务代码。而且安全领域有大量容易被忽视的细节（如 CSRF、CORS 配置不当），手写容易留下漏洞。

**设计动机：** Spring Security 把这些安全需求抽象为一组可组合的组件。你只需声明规则，框架负责执行。

### 没有 Spring Security 会怎样

**困境：** 用 Servlet Filter 或 Spring Interceptor 手写安全逻辑。初期简单，但随着需求增加，认证方式多样化、权限粒度细化和安全威胁防护需求增加，手写的安全代码会变得脆弱且难以审计。常见的自制安全问题包括：密码明文存储、Session 固定攻击未防护、CSRF 未关闭或配置错误、Token 验证逻辑有漏洞等。

**有了 Spring Security 之后：** 经过 20 年实战检验的安全框架，覆盖了绝大多数安全场景。默认行为已经是安全的（密码自动 BCrypt 加密、CSRF 保护默认开启、Session 固定保护默认生效）。你只需要配置规则。

## 概念深入解释

### SecurityFilterChain 架构

Spring Security 的核心是一条 **过滤器链（Filter Chain）**。每个 HTTP 请求进入 Servlet 容器后，依次经过这条链上的一系列过滤器，每个过滤器负责一项安全检查。

```
HTTP 请求
    │
    ▼
┌─────────────────────────────────────────────────────┐
│  SecurityFilterChain                                │
│                                                     │
│  ┌──────────────────┐    ┌──────────────────┐      │
│  │ SecurityContext   │ →  │ UsernamePassword │ →  . │
│  │ PersistenceFilter │    │ AuthenticationFilter│   │
│  └──────────────────┘    └──────────────────┘      │
│                                                     │
│  ... 更多过滤器 ...                                   │
│                                                     │
│  ┌──────────────────┐    ┌──────────────────┐      │
│  │ ExceptionTranslation│ → │ Authorization   │      │
│  │ Filter            │    │ Filter           │      │
│  └──────────────────┘    └──────────────────┘      │
└─────────────────────────────────────────────────────┘
    │
    ▼
DispatcherServlet → Controller
```

**关键过滤器（按执行顺序）：**

| 过滤器 | 职责 |
|--------|------|
| `SecurityContextPersistenceFilter` | 从 Session 恢复 SecurityContext 或在请求结束后持久化 |
| `UsernamePasswordAuthenticationFilter` | 处理表单登录请求（默认 POST /login） |
| `BasicAuthenticationFilter` | 处理 HTTP Basic 认证 |
| `ExceptionTranslationFilter` | 将认证/授权异常转换为 HTTP 响应（401/403） |
| `FilterSecurityInterceptor` | 最后一道检查：当前用户是否有权限访问该资源 |

### 核心组件关系

```
SecurityFilterChain ─ 引用 ─→ SecurityConfigurer
                                  │
                                  ├─→ AuthenticationManager
                                  │       │
                                  │       └─→ AuthenticationProvider(s)
                                  │               │
                                  │               ├─ UserDetailsService (查用户)
                                  │               └─ PasswordEncoder (验密码)
                                  │
                                  └─→ SecurityContextHolder (存储认证结果)
                                          │
                                          └─→ SecurityContext
                                                  │
                                                  └─→ Authentication
                                                          │
                                                          ├─ Principal (用户信息)
                                                          ├─ Credentials (密码/Token)
                                                          └─ Authorities (权限列表)
```

### 默认安全配置

Spring Boot 的自动配置会提供一个**默认安全配置**：

- 所有请求都需要认证
- 自动生成一个表单登录页（`/login`）
- 自动生成一个默认用户（username: `user`，密码在启动日志中随机生成）
- 开启 CSRF 保护
- 开启 Session 固定保护

这个默认配置在开发阶段很烦人 — 每次启动密码都变，API 测试不方便。大多数项目的第一步就是覆盖默认配置。

### 自定义安全配置

Spring Security 5.7+ 推荐使用 Lambda DSL 配置 `SecurityFilterChain`：

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
            );
        return http.build();
    }
}
```

### 认证流程

```
用户提交登录表单（username + password）
    │
    ▼
UsernamePasswordAuthenticationFilter
    │  创建 UsernamePasswordAuthenticationToken (未认证)
    ▼
AuthenticationManager
    │
    ▼
AuthenticationProvider
    ├── UserDetailsService.loadUserByUsername(username)
    │       └── 返回 UserDetails (包含密码、权限等)
    │
    └── PasswordEncoder.matches(rawPassword, encodedPassword)
            └── 密码匹配成功
                │
                ▼
            返回已认证的 Authentication 对象
                │
                ▼
        SecurityContextHolder.getContext().setAuthentication(auth)
                │
                ▼
        请求继续处理（当前用户已被认证）
```

### 密码编码器

Spring Security 强制要求密码必须加密存储。`PasswordEncoder` 是密码编码的抽象：

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**常见的 PasswordEncoder 实现：**

| 实现 | 算法 | 推荐程度 |
|------|------|----------|
| `BCryptPasswordEncoder` | BCrypt（带 salt） | **推荐**，Spring Security 默认 |
| `SCryptPasswordEncoder` | SCrypt | 内存敏感场景 |
| `Pbkdf2PasswordEncoder` | PBKDF2 | FIPS 合规场景 |
| `DelegatingPasswordEncoder` | 委托模式，支持多种编码 | 迁移遗留密码方案时使用 |
| `NoOpPasswordEncoder` | 明文 | 仅测试用，**禁止生产使用** |

### SecurityContext 与线程绑定

`SecurityContextHolder` 默认使用 `ThreadLocal` 策略存储当前用户的安全上下文，这意味着每个请求线程都有独立的 `SecurityContext`。获取当前用户信息：

```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();  // 当前用户名
```

在 Controller 中更简洁的方式：

```java
@GetMapping("/me")
public User me(@AuthenticationPrincipal UserDetails user) {
    // user 就是当前登录用户
}
```

## 核心要点

1. **Spring Security 的核心是 Filter Chain：** 请求在到达 Controller 前经过一系列过滤器完成安全检查。
2. **认证（Authentication）和授权（Authorization）是两件事：** 先确认你是谁，再确认你能做什么。
3. **覆写默认配置而非从零构建：** Spring Boot 已有安全的默认值，在上面修改比从零搭建更安全。
4. **密码必须加密存储：** 使用 `BCryptPasswordEncoder`，永远不要存明文或简单 MD5。
5. **SecurityContext 是线程绑定的：** 每个请求在自己的线程中，有独立的认证信息，不会互相污染。
6. **默认配置会保护所有端点：** 如果发现所有 API 返回 401，检查是否覆盖了 `permitAll()` 规则。

## 常见误区

- **关闭 CSRF 保护解决 403 报错，但不理解为什么。** CSRF 保护默认开启，POST/PUT/DELETE 请求需要携带 CSRF Token。如果是纯 RESTful API（前后端分离 + JWT 认证），关闭 CSRF 是合理的，因为 Token 本身已防 CSRF。但如果是基于 Session 的 Web 应用，关闭 CSRF 会留下安全隐患。
- **把 Spring Security 的 `UserDetailsService` 和应用的 `UserService` 混淆。** `UserDetailsService` 是安全框架的接口，只负责根据用户名加载用户信息给框架做认证检查。不要在其中写业务逻辑（如登录日志、积分发放），那些应该放在认证成功的回调中。
- **在 `SecurityFilterChain` 配置中没有正确排序 `requestMatchers`。** Spring Security 按 matcher 声明顺序匹配。如果把 `anyRequest().authenticated()` 放在前面，后面的 `.permitAll()` 规则都不会被应用。顺序应为：最具体的规则 → 次具体的规则 → `anyRequest()`。
- **直接在 `application.yml` 中写明文密码。** 任何明文配置都会被记录在版本控制和日志中。即使是测试环境，也应用环境变量或 `{bcrypt}hash` 格式。
- **不区分401和403：** 401 Unauthorized 表示"未认证"（你还没登录），403 Forbidden 表示"无权限"（你登录了但权限不够）。很多人用 403 覆盖所有安全错误，会让前端无法判断是该跳转登录页还是提示无权限。

## 与其他概念的关联

- **前置：** [Java Spring Filter 链原理](./17_Java%20Spring%20请求处理.md) -- Security Filter Chain 基于标准 Servlet Filter，理解请求处理流程是前提
- **前置：** [Java Spring AOP](./08_Java%20Spring%20AOP.md) -- Spring Security 的 `@PreAuthorize` 等方法安全通过 AOP 实现
- **前置：** [Java Spring Boot 自动配置](./11_Java%20Spring%20Boot%20自动配置.md) -- Spring Security 的默认行为由自动配置提供
- **并行：** [Java Spring 认证与授权](./39_Java%20Spring%20认证与授权.md) -- 深入认证方式与权限模型
- **并行：** [Java Spring JWT](./40_Java%20Spring%20JWT.md) -- 前后端分离架构中 JWT 取代 Session 实现无状态认证
- **并行：** [Java Spring Casbin](./29_Java%20Spring%20Casbin.md) -- 当权限模型更复杂时，Casbin 作为 Spring Security 的补充
- **后续：** [Java Spring Cloud Gateway](../Spring_Cloud/Java Spring Cloud Gateway.md) -- 微服务架构中网关层统一处理认证
