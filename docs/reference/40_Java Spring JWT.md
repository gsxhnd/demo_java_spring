---
title: Java Spring JWT
created: 2026-05-08 22:29:19
category: Java-Spring
tags:
  - Java
  - Spring
  - Security
  - JWT
  - Token
  - 无状态认证
---

<!-- markdownlint-disable MD025 -->

# Java Spring JWT

## 为什么要学 JWT

前面我们学了 Spring Security 的认证与授权，了解了表单登录 + Session 的经典模式。但这种方式有一个前提：客户端是浏览器，浏览器会自动管理 Cookie，服务端用 Session 维持登录状态。当你面对前后端分离架构（Vue/React 前端独立部署，通过 API 与后端通信）或移动 App 时，Cookie + Session 模式就不太适用了 — 移动端不自动管理 Cookie，跨域请求需要额外配置，而且 Session 把"状态"留在了服务端，使得水平扩展（新增服务实例）时需要 Session 共享。

JWT（JSON Web Token）是解决这些问题的标准方案。它是一个自包含的 Token — 用户身份信息编码在 Token 内部，服务端不需要存储 Session 来记住"谁登录了"。这也意味着每个服务实例都可以独立验证 Token，不需要共享 Session 存储。

## 核心概念

### JWT 是什么

JWT（JSON Web Token）是一种紧凑的、自包含的、基于 JSON 的令牌格式，用于在各方之间安全传输声明信息。它由三部分组成：Header（头部）、Payload（载荷）、Signature（签名），用 Base64URL 编码后用点号连接。

**换个说法：** Session 模式像去健身房办卡 — 卡上只有一个编号，你的会员信息存在健身房的系统里。JWT 模式像身份证 — 卡上直接印了你的姓名、出生日期、有效期，而且有防伪水印。任何人看到这张身份证都能验证真伪并读取信息，不需要联网查询中心数据库。

### 为什么需要 JWT

**痛点场景：** 你的后端部署了 3 个实例，前面有负载均衡。用户登录到实例 A，Session 存在实例 A 的内存中。下一次请求被负载均衡分到实例 B，实例 B 没有这个用户的 Session，于是返回 401，要求重新登录。为了解决问题，你需要额外引入 Redis 做 Session 共享，增加了架构复杂度和调用延迟。

**设计动机：** JWT 是无状态的 — Token 本身就包含了所有验证所需的信息。任何服务实例拿到 Token 都能独立验证，不需要访问共享存储。

### 没有 JWT 会怎样

**困境：** 前后端分离 + 多实例部署时，Session 共享是绕不开的额外工作。移动 App 里需要手动处理 Cookie 存储。跨域请求配置 CORS + withCredentials 的过程也充满坑。

**有了 JWT 之后：** 前端把 Token 存在 localStorage（或内存/HttpOnly Cookie），每次请求加到 `Authorization: Bearer <token>` 头。后端任何实例都能独立验证。不需要 Session，不需要 Cookie，不需要跨实例状态共享。

## 概念深入解释

### JWT 结构

一个 JWT 看起来像这样：

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZSJ9.L8fGmjwUOZmBVeTImVQhNIqRPEY0HIHF
|______ Header ______|____ Payload ____|_________ Signature __________|
```

**Header（头部）：** 声明签名算法和 Token 类型。

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload（载荷）：** 存放声明（Claims），即你想要传递的信息。

```json
{
  "sub": "alice",           // 主题（通常是用户ID）
  "iat": 1680000000,        // 签发时间
  "exp": 1680003600,        // 过期时间
  "roles": ["USER"]         // 自定义声明
}
```

**Signature（签名）：** 用 Header 指定的算法，对 `Base64(Header).Base64(Payload)` 计算签名。服务端用相同的密钥验证签名是否合法，防止 Token 被篡改。

### JWT 认证流程

```
1. 用户登录 (POST /login {username, password})
    │
    ▼
2. 服务端验证用户名密码
    │
    ▼
3. 生成 JWT Token（包含用户 ID、角色、过期时间），签名后返回
    │
    ▼
4. 前端存储 Token（localStorage / Cookie）
    │
    ▼
5. 后续请求：Authorization: Bearer <token>
    │
    ▼
6. 服务端自定义 Filter 截取 Token
    │
    ▼
7. 验证签名 → 解析 Payload → 构造 Authentication 对象
    │
    ▼
8. 将 Authentication 设置到 SecurityContextHolder
    │
    ▼
9. 请求继续进入 Controller
```

### 在 Spring Security 中集成 JWT

JWT 不是 Spring Security 内置的认证方式，需要手动添加一个 Filter：

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);  // 从 Authorization 头提取
        if (token != null && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
```

然后在 SecurityConfig 中注册这个 Filter：

```java
http.addFilterBefore(
    jwtAuthenticationFilter,
    UsernamePasswordAuthenticationFilter.class
);
```

### Access Token vs Refresh Token

单用一个 Token 的问题：如果设置 15 分钟过期（安全），用户每 15 分钟就要重新登录（体验差）。如果设置 7 天过期，Token 泄露后有长达 7 天的风险窗口。双 Token 方案解决这个矛盾：

| 类型 | 有效期 | 存储位置 | 作用 |
|------|--------|----------|------|
| Access Token | 短（15-30 分钟） | 内存（不持久化） | 日常 API 请求认证 |
| Refresh Token | 长（7-30 天） | HttpOnly Cookie 或安全存储 | 获取新的 Access Token |

流程：Access Token 过期 → 前端用 Refresh Token 请求 `/refresh` 接口 → 服务端返回新的 Access Token。Refresh Token 只在刷新时使用，暴露面极小。

### JWT 常见安全问题

**Token 泄露后的处理：** JWT 的无状态性是一把双刃剑 — 服务端无法主动"撤销"一个已签发的 Token（不像 Session 可以直接销毁）。应对策略：

- **短有效期 Access Token：** 即使泄露，影响窗口只有 15 分钟
- **Token 黑名单：** 维护一个 Redis 黑名单，服务端在验证时检查，但这样又引入了状态存储
- **Refresh Token Rotation：** 每次使用 Refresh Token 时发行一个新的，旧 Token 的单次使用机制限制泄露风险

**Payload 不是加密的：** JWT 的签名防止篡改，但不防止读取。Base64URL 编码是可逆的，任何人都可以解码看到 Payload 内容。**绝对不要在 JWT Payload 中存敏感信息**（如密码）。

**签名算法选择：** HS256（HMAC + SHA256）用同一个密钥签名和验证，适合单实例或共享密钥的场景。RS256（RSA + SHA256）用私钥签名、公钥验证，适合微服务架构（认证服务有私钥，其他服务只有公钥）。

## 核心要点

1. **JWT 是无状态 Token：** 服务端不需要存储 Session，每个实例独立验证，天然支持水平扩展。
2. **双 Token 机制：** Access Token（短时）+ Refresh Token（长时）兼顾安全与体验。
3. **Payload 不加密只防篡改：** 不要在 JWT 中存放密码、身份证号等敏感信息。
4. **用 Filter 集成 JWT：** 自定义 `OncePerRequestFilter`，在 Security Filter Chain 中注册，Spring Security 感知不到 JWT 的存在。
5. **妥善保管密钥：** JWT 签名的安全完全依赖于密钥的保密。密钥泄露意味着任何人都能伪造 Token。

## 常见误区

- **在 JWT Payload 中存放密码、手机号等敏感信息。** Base64URL 编码不是加密，任何人都能看到。JWT 只能保证"内容未被篡改"，不能保证"内容不可见"。敏感信息永远不放 JWT。
- **设置超长过期时间（30 天甚至永久）。** Token 一旦签发就无法主动撤销。过期时间越长，泄露后的风险窗口越大。Access Token 建议 15-30 分钟，Refresh Token 不超过 30 天并配合 Rotation。
- **用 localStorage 存储 Token 后不加 XSS 防护。** localStorage 可以被同域的任意 JavaScript 代码读取。如果应用存在 XSS 漏洞，攻击者可以轻易窃取 Token。推荐 Refresh Token 使用 HttpOnly Cookie 存储（JS 不可读），Access Token 在内存中。
- **认证失败时返回模糊错误信息。** 返回 `Invalid credentials` 而不是 `User not found` 或 `Wrong password`，避免攻击者枚举用户名。但调试时可以在日志中记录详细原因。
- **JWT 签名验证只检查格式不检查签名。** 有些 DIY 实现只解析了 Token 内容，跳过了签名验证。这样的"JWT"完全可被伪造。验证的两个关键操作缺一不可：`parseClaimsJws`（验证签名）和 `isBeforeExpiration`（检查过期）。
- **Refresh Token 存在前端。** Refresh Token 的生命周期长，泄露风险更大。使用 HttpOnly + Secure + SameSite Cookie 存储 Refresh Token，只在刷新接口上使用，日常 API 只用 Access Token。

## 与其他概念的关联

- **前置：** [Java Spring Security](./38_Java%20Spring%20Security.md) -- JWT 通过自定义 Filter 集成到 Security Filter Chain
- **前置：** [Java Spring 认证与授权](./39_Java%20Spring%20认证与授权.md) -- JWT 是认证方式之一，与表单登录、OAuth2 等并列
- **前置：** [Java Spring Boot 配置](./14_Java%20Spring%20Boot%20配置.md) -- JWT 密钥和相关参数通过配置文件管理
- **并行：** [Java Spring Casbin](./29_Java%20Spring%20Casbin.md) -- JWT 的 Payload 中携带角色/权限，Casbin 根据这些信息做授权检查
- **后续：** [Java Spring Cloud Gateway](../Spring_Cloud/Java Spring Cloud Gateway.md) -- 网关层统一做 JWT 验证，内部服务可以跳过认证环节
