# Spring Security + JWT — 认证授权 / Authentication & Authorization

> Spring Security 过滤器链、JWT 无状态认证、RBAC 权限模型

## 1. 概述 / Overview

Spring Security 是 Spring 生态的安全框架，提供认证（Authentication）和授权（Authorization）能力。结合 JWT（JSON Web Token）可实现无状态的分布式认证方案。

### 认证 vs 授权

| 概念 | 说明 | 回答的问题 |
|---|---|---|
| 认证 (Authentication) | 验证"你是谁" | 用户名密码是否正确？Token 是否有效？ |
| 授权 (Authorization) | 验证"你能做什么" | 该用户是否有权限访问此接口？ |

### 常见认证方案对比

| 方案 | 状态 | 适用场景 |
|---|---|---|
| Session + Cookie | 有状态 | 传统单体 Web 应用 |
| **JWT** | 无状态 | 前后端分离、微服务、移动端 |
| OAuth2 | 无状态 | 第三方登录、开放平台 |
| OAuth2 + JWT | 无状态 | 微服务统一认证（推荐） |

---

## 2. 核心概念 / Core Concepts

### Spring Security 过滤器链

```
HTTP Request
     │
     ▼
┌─────────────────────────────────────────┐
│         Security Filter Chain            │
│                                          │
│  ┌─────────────────────────────────┐    │
│  │ CorsFilter                      │    │  ← 跨域处理
│  ├─────────────────────────────────┤    │
│  │ CsrfFilter                     │    │  ← CSRF 防护（API 通常禁用）
│  ├─────────────────────────────────┤    │
│  │ UsernamePasswordAuthFilter      │    │  ← 表单登录（或自定义 JWT Filter）
│  ├─────────────────────────────────┤    │
│  │ BearerTokenAuthFilter          │    │  ← Bearer Token 认证
│  ├─────────────────────────────────┤    │
│  │ AuthorizationFilter            │    │  ← 权限校验
│  ├─────────────────────────────────┤    │
│  │ ExceptionTranslationFilter     │    │  ← 异常处理（401/403）
│  └─────────────────────────────────┘    │
│                                          │
└──────────────────┬──────────────────────┘
                   │
                   ▼
            Controller (业务逻辑)
```

### JWT 结构

```
Header.Payload.Signature

Header:    {"alg": "HS256", "typ": "JWT"}
Payload:   {"sub": "user123", "roles": ["ADMIN"], "exp": 1700000000}
Signature: HMACSHA256(base64(header) + "." + base64(payload), secret)
```

| 部分 | 说明 |
|---|---|
| Header | 算法和类型 |
| Payload | 用户信息（Claims）：用户 ID、角色、过期时间 |
| Signature | 签名，防篡改 |

### JWT 认证流程

```
1. 登录：POST /auth/login {username, password}
   → 验证成功 → 生成 JWT Token → 返回给客户端

2. 请求：GET /api/users  Header: Authorization: Bearer <token>
   → JWT Filter 解析 Token → 验证签名和过期时间
   → 提取用户信息 → 设置 SecurityContext → 放行

3. 授权：检查用户角色是否匹配接口要求的权限
```

### RBAC 权限模型

```
User ──→ Role ──→ Permission

用户 zhangsan ──→ 角色 ADMIN ──→ 权限 user:read, user:write, order:*
用户 lisi    ──→ 角色 USER  ──→ 权限 user:read
```

---

## 3. 快速集成 / Quick Start

### Maven 依赖

- `spring-boot-starter-security` — Security 核心
- `spring-boot-starter-oauth2-resource-server` — OAuth2 资源服务器（JWT 验证）
- 或 `io.jsonwebtoken:jjwt-api` + `jjwt-impl` + `jjwt-jackson` — JJWT 库（手动 JWT）

### 关键配置

| 配置 | 说明 |
|---|---|
| `SecurityFilterChain` Bean | 定义安全规则（哪些路径放行、哪些需认证） |
| `PasswordEncoder` Bean | 密码加密器（BCryptPasswordEncoder 推荐） |
| `UserDetailsService` | 用户信息加载（从数据库查询用户） |
| `JwtDecoder` Bean | JWT 解码器（OAuth2 Resource Server 方式） |

### 权限控制方式

| 方式 | 说明 |
|---|---|
| URL 级别 | `requestMatchers("/admin/**").hasRole("ADMIN")` |
| 方法级别 | `@PreAuthorize("hasRole('ADMIN')")` |
| 表达式 | `@PreAuthorize("hasAuthority('user:write')")` |

---

## 4. 进阶要点 / Advanced Topics

- **JWT 刷新机制**：Access Token（短期，15min）+ Refresh Token（长期，7d），双 Token 方案
- **Token 黑名单**：JWT 无法主动失效，用 Redis 存储已注销的 Token
- **OAuth2 授权服务器**：Spring Authorization Server 搭建统一认证中心
- **多租户认证**：根据租户 ID 切换认证策略
- **接口级权限**：`@PreAuthorize` + SpEL 表达式，细粒度控制
- **CORS 配置**：Security 中配置 `cors()`，与 Gateway CORS 配合
- **CSRF 防护**：前后端分离 API 通常禁用 CSRF（`csrf().disable()`）
- **密码加密**：BCrypt（推荐）、Argon2、SCrypt

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| 所有接口都返回 401 | 检查 SecurityFilterChain 配置，确认放行了登录接口 |
| CORS 跨域被拦截 | Security 中配置 `cors()`，不要只在 Controller 加 `@CrossOrigin` |
| `@PreAuthorize` 不生效 | 确认启用了 `@EnableMethodSecurity` |
| JWT 过期后无法刷新 | 实现 Refresh Token 机制 |
| 密码明文存储 | 必须使用 `PasswordEncoder`，推荐 BCrypt |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-security-demo/`](../../examples/spring-security-demo/)

**演示功能：**
- Spring Security 过滤器链配置
- JWT Token 生成与验证
- 用户注册与登录
- RBAC 权限控制（USER/ADMIN 角色）
- `@PreAuthorize` 方法级权限注解
- 全局异常处理

**运行示例：**
```bash
cd examples/spring-security-demo
mvn spring-boot:run
```

**API 接口：**
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/public/**` - 公开接口
- `GET /api/user/**` - 需要 USER 角色
- `GET /api/admin/**` - 需要 ADMIN 角色

## 7. 参考链接 / References

- [Spring Security 官方文档](https://docs.spring.io/spring-security/reference/)
- [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/)
- [JWT.io](https://jwt.io/)
- [JJWT GitHub](https://github.com/jwtk/jjwt)
