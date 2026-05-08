---
title: Java Spring 认证与授权
created: 2026-05-08 22:29:19
category: Java-Spring
tags:
  - Java
  - Spring
  - Security
  - Authentication
  - Authorization
  - Role
  - Permission
---

<!-- markdownlint-disable MD025 -->

# Java Spring 认证与授权

## 为什么要学认证与授权

上一节我们了解了 Spring Security 的整体架构 — Filter Chain 如何拦截请求、SecurityContextHolder 如何存储认证信息。但你可能会问：框架知道请求经过了 Filter Chain，但它怎么知道"当前用户是谁"（认证）和"这个用户能不能访问这个接口"（授权）？

认证和授权是 Spring Security 的两个核心功能，它们回答了安全领域的两个基本问题。理解了这两者的具体运作机制，你才能真正定制出符合业务需求的安全方案，而不是对着 `403 Forbidden` 报错一筹莫展。

## 核心概念

### 认证（Authentication）是什么

认证是确认用户身份的过程 — 回答"你是谁"这个问题。它通常通过凭证（用户名+密码、Token、证书等）来验证用户的身份声明。

**换个说法：** 认证就是门卫查身份证。你说你是张三，门卫看你的身份证照片和你本人是否一致，确认后放行。身份证就是你的凭证（Credential），门卫的确认过程就是认证。

### 为什么需要认证

没有认证的应用无法区分用户。所有请求都是匿名的，你无法实现"我的订单"、"个人设置"这类基于用户身份的功能。认证是所有个性化功能和安全控制的基础。

### 授权（Authorization）是什么

授权是决定已认证用户能做什么的过程 — 回答"你能做什么"这个问题。在确认了用户身份后，根据其角色或权限决定是否允许访问某个资源或执行某个操作。

**换个说法：** 授权就是门禁卡权限。门卫查过身份证确认你是张三（认证），但你的工卡只能打开 3 楼（普通员工楼层），打不开 5 楼（高管楼层）。工卡的楼层权限就是授权。

### 为什么需要授权

认证只告诉系统"用户是张三"，但系统还需要知道"张三能不能删除商品"、"张三能不能看所有用户的订单"。授权让你精细化控制每个用户的能力边界，实现最小权限原则。

### 没有认证与授权会怎样

**困境：** 所有接口对所有人可见。一个知道 URL 的人可以直接调用删除用户接口、导出敏感数据接口。即使你手写了一个简单的登录检查，权限控制也会退化为仅在几个关键接口上做 `if (user.isAdmin())` 判断，不一致且容易遗漏。

**有了认证与授权之后：** 认证在入口统一完成，授权规则声明式配置（"`/admin/**` 需要 ADMIN 角色"），框架在每次请求时自动检查。新增接口默认被保护，不需要开发者"记得"加安全检查。

## 概念深入解释

### 认证方式的几种形态

| 认证方式 | 机制 | 适用场景 |
|----------|------|----------|
| **表单登录** | 浏览器 POST 用户名+密码，服务端返回 Set-Cookie（Session） | 传统的服务端渲染 Web 应用 |
| **HTTP Basic** | 请求头携带 `Authorization: Basic base64(user:pass)` | 内部工具、API 调试、极简场景 |
| **JWT Token** | 登录后返回签名 Token，后续请求在 `Authorization: Bearer <token>` 中携带 | 前后端分离、移动 App、无状态服务 |
| **OAuth2 / OIDC** | 委托第三方（Google、GitHub、微信）认证 | 社交登录、企业 SSO |
| **API Key** | 预分发的密钥，在请求头中携带 | 机器间调用、开放平台 API |

### UserDetailsService

`UserDetailsService` 是 Spring Security 加载用户信息的核心接口。它只有一个方法：

```java
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException;
}
```

Spring Security 只关心两点：用户名是否存在、密码是否匹配。你的职责是实现 `loadUserByUsername`，从数据库（或其他用户存储）加载用户信息并包装为 `UserDetails` 返回：

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found: " + username));

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())         // 已加密的密码
            .roles(user.getRoles().toArray(new String[0]))
            .build();
    }
}
```

### 授权模型对比

Spring Security 支持三种粒度的授权：

**1. URL 级别授权（Web Security）**

在 `SecurityFilterChain` 中声明，控制哪些 URL 模式需要哪些角色：

```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/public/**").permitAll()
    .requestMatchers("/api/users/**").hasRole("USER")
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
    .anyRequest().authenticated()
);
```

**2. 方法级别授权（Method Security）**

用注解在 Service 或 Controller 方法上声明权限要求。需要先启用：`@EnableMethodSecurity`（Spring Security 6+）。

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long userId) { ... }

@PreAuthorize("hasRole('USER') and #userId == authentication.principal.id")
public UserProfile getUserProfile(Long userId) { ... }  // 只能看自己的

@PostAuthorize("returnObject.owner == authentication.name")
public Document getDocument(Long id) { ... }  // 只能看自己的文档
```

**3. 实例级别授权（ACL）**

控制特定领域对象实例的访问（如"文档 #42 只能被作者和已授权的协作者访问"）。需要额外引入 `spring-security-acl` 模块。

### 角色 vs 权限

| 维度 | 角色 (Role) | 权限 (Permission / Authority) |
|------|------------|------------------------------|
| 概念层级 | 一组权限的集合 | 单个原子操作 |
| 表达方式 | `ROLE_ADMIN`、`ROLE_USER` | `READ_PRIVILEGES`、`WRITE_PRIVILEGES` |
| 配置方式 | `.hasRole("ADMIN")` | `.hasAuthority("READ_PRIVILEGES")` |
| 灵活性 | 粗粒度，改角色需要重新部署 | 细粒度，可在运行时动态分配 |
| 使用建议 | 简单应用、角色固定 | 复杂权限模型、需要动态控制 |

大多数应用从角色起步：`USER` 和 `ADMIN` 足够覆盖初期需求。当权限模型变复杂（如"市场部经理可以看但不能改财务报表"），考虑从角色过渡到权限，或引入 Casbin 这类专业的授权库。

### 自定义认证流程示例

假设你需要同时支持用户名+密码登录和手机号+验证码登录，可以注册多个 `AuthenticationProvider`：

```java
@Bean
public AuthenticationManager authManager(HttpSecurity http) throws Exception {
    return http.getSharedObject(AuthenticationManagerBuilder.class)
        .authenticationProvider(usernamePasswordProvider())
        .authenticationProvider(smsCodeProvider())
        .build();
}
```

Spring Security 会遍历所有 Provider，找到能处理当前认证请求的那个。

### 认证成功/失败处理器

你可以自定义认证成功或失败后的行为（如返回 JSON 而非重定向，记录登录日志）：

```java
http.formLogin(form -> form
    .successHandler((request, response, auth) -> {
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"ok\"}");
    })
    .failureHandler((request, response, exception) -> {
        response.setStatus(401);
        response.getWriter().write("{\"error\":\"Invalid credentials\"}");
    })
);
```

## 核心要点

1. **认证回答"你是谁"，授权回答"你能做什么"：** 两个独立但紧密关联的概念，框架对两者有分离的组件设计。
2. **UserDetailsService 是用户加载的唯一入口：** 实现它，从任何数据源（DB、LDAP、API）加载用户信息。
3. **三层授权粒度：** URL 级别最粗，方法级别中等，ACL 最细。从 URL 级别开始，不够再升级。
4. **从角色开始，按需过渡到权限：** 不要过早优化为细粒度权限模型，大多数场景下角色足够。
5. **`@PreAuthorize` 支持 SpEL 表达式：** 可以做动态权限检查（如"只能访问自己的数据"），不只是静态角色检查。

## 常见误区

- **在 UserDetailsService 中写业务逻辑。** `loadUserByUsername` 只负责加载用户信息给框架认证。登录日志、更新最后登录时间等逻辑应该通过 `AuthenticationSuccessHandler` 或事件监听器实现，不要污染安全接口。
- **混淆 `hasRole` 和 `hasAuthority`。** `hasRole("ADMIN")` 等价于 `hasAuthority("ROLE_ADMIN")` — hasRole 自动加 `ROLE_` 前缀。如果你在数据库存的是 `ROLE_ADMIN`，用 `hasRole("ADMIN")`；如果存的是 `ADMIN`，必须用 `hasAuthority("ADMIN")`。
- **认为配置了方法级别安全就不需要 URL 级别安全。** 理论上 URL 安全先执行、方法安全后执行，两层可以共存。但最佳实践是：URL 级别做粗粒度入口控制（减少进入应用的不合法请求），方法级别做细粒度业务权限控制。但大多数团队选其一为主。
- **认证成功后 SecurityContextHolder 为空。** 常见于 `@Async` 方法或不同线程中。`SecurityContextHolder` 默认使用 `ThreadLocal`，子线程中无法访问父线程的安全上下文。如果需要在异步线程中访问当前用户，设置 `SecurityContextHolder.setStrategyName(MODE_INHERITABLETHREADLOCAL)` 或显式传递。
- **自定义 Filter 放在 Security Filter Chain 之外导致不生效或顺序错误。** 应该通过 `http.addFilterBefore(myFilter, UsernamePasswordAuthenticationFilter.class)` 或 `addFilterAfter` 将自定义 Filter 插入到 Security Filter Chain 中正确的位置，而不是注册一个全局 Filter。

## 与其他概念的关联

- **前置：** [Java Spring Security](./38_Java%20Spring%20Security.md) -- 整体架构和 Filter Chain 是认证授权的运行基础
- **前置：** [Java Spring 请求处理](./17_Java%20Spring%20请求处理.md) -- Security Filter Chain 基于 Servlet Filter 机制
- **前置：** [Java Spring 配置管理](./31_Java%20Spring%20配置管理.md) -- 安全配置通常随 Profile 环境变化（dev 宽松，prod 严格）
- **并行：** [Java Spring JWT](./40_Java%20Spring%20JWT.md) -- JWT 是前后端分离架构中最常用的无状态认证方案
- **并行：** [Java Spring Casbin](./29_Java%20Spring%20Casbin.md) -- 当角色模型不够用时，Casbin 提供更灵活的策略管理
- **后续：** [Java Spring Cloud Gateway](../Spring_Cloud/Java Spring Cloud Gateway.md) -- 微服务中网关统一处理认证，内部服务可以简化
