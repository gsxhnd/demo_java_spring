---
title: Java Spring Casbin
created: 2026-05-08 22:21:06
category: Java-Spring
tags:
  - Java
  - Spring
  - Security
  - Casbin
  - RBAC
  - Authorization
---

<!-- markdownlint-disable MD025 -->

# Java Spring Casbin

## 为什么要学 Casbin

上一节讲完了 Spring Security + JWT 的认证方案，你已经掌握了"你是谁"和"你能做什么"的基础实现。Spring Security 框架本身也支持角色和权限的管理，可以覆盖大部分常见需求。但随着权限模型变得越来越复杂，你会发现基于注解或 Java 代码硬编码的权限管理方式会逐渐吃力。Casbin 就是为了解决这个问题而出现的。

在 Spring Security 中，权限通常通过以下几种方式控制：`@PreAuthorize("hasRole('ADMIN')")` 直接硬编码角色、显式的 URL 安全配置、或者存储角色和资源映射关系的数据库表驱动授权。这些方式在权限模型简单时可行，但当权限逻辑变得复杂时，维护成本会急剧上升。

Casbin 的价值在于将"权限判断逻辑"与"策略数据存储"分离。你可以用配置文件定义权限模型（RBAC、ABAC 或自定义规则），策略数据存储在任何介质（数据库、文件、Redis），并且支持运行时动态修改策略。

## 核心概念

### Casbin 是什么

Casbin 是一个开源的、支持多种访问控制模型的授权库。它不是框架，不绑定语言或存储，核心只做一件事：**给定一个"谁、什么、怎么做"的访问请求，返回允许或拒绝。**

它的独特之处在于把访问控制拆成两个正交的部分：

- **Model（模型）** — 定义权限判断的逻辑结构，写在一个配置文件中（如 `model.conf`）
- **Policy（策略）** — 定义具体的权限数据，存储来源可以是文件、数据库、Redis、etcd 等

类比：Casbin 就像交通信号灯系统。Model 定义了规则（红灯停、绿灯行），Policy 存储了每盏灯在哪个路口、指向哪个车道。你走到一个路口问"我能过吗"，Casbin 根据模型和策略给你明确的答复。

```ini
# model.conf — 定义 RBAC 模型的规则结构
[request_definition]
r = sub, obj, act

[policy_definition]
p = sub, obj, act

[role_definition]
g = _, _

[policy_effect]
e = some(where (p.eft == allow))

[matchers]
m = g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act
```

```csv
# policy.csv — 定义具体的用户、角色、权限数据
p, admin, /api/users, GET
p, admin, /api/users, POST
p, user, /api/users, GET
g, alice, admin
g, bob, user
```

### 为什么需要 Casbin

Spring Security 在复杂权限场景下有四个痛点：

1. **权限规则硬编码** — `@PreAuthorize("hasRole('ADMIN')")` 中的角色名在代码中写死，无法运行时动态调整
2. **异构权限模型难统一** — 一个微服务系统里，认证用 OAuth2，授权可能涉及角色、资源属性和环境条件等多种维度。Spring Security 本身没有统一的跨模型权限框架
3. **多租户隔离** — 用户 A 在租户 X 下是 admin，在租户 Y 下只是 viewer。Spring Security 的角色体系处理这种"域内角色"比较笨拙
4. **策略变更需要重新部署** — 改了权限规则就要改代码、重启服务

Casbin 解决了这些问题：

- **权限规则从代码中抽离** — 富客户端的权限模型中，策略数据存储在数据库中，你可以通过管理界面直接修改权限，无需改动代码或重启服务
- **统一的权限模型** — 同一个 Casbin 引擎可以处理 RBAC、ABAC、ACL等多种模型，甚至自定义混合模型
- **原生支持域隔离** — 内置 domain 字段，通过 `g, user, role, domain` 实现多租户角色隔离
- **动态生效** — 策略数据存储在数据库中时，修改即生效，不需要重启

### 没有 Casbin 会怎样

没有 Casbin，面对复杂权限需求时：

- 权限逻辑散落在注解、拦截器、Security 配置、数据库查询等多个地方，整个授权体系难以统一管理
- 新增一个角色或调整一个权限，需要修改多处代码并重新部署
- 多租户场景需要自己实现一套域-角色映射逻辑
- 权限审计和可视化几乎不可能，因为你无法从代码中提取出一份完整的权限矩阵

有了 Casbin：权限规则集中定义在一个模型文件中；所有策略数据存储在一处；添加权限、修改角色、查询"谁可以做什么"都变成模型+策略的查询操作；权限变更是动态的、可审计的、可可视化的。

## 概念深入解释

### Casbin 的核心组件

Casbin 的设计围绕三个核心组件展开：

```
┌──────────────────────────────────────────────────┐
│                    Casbin Enforcer                 │
│                                                    │
│  enforcer.enforce("alice", "/api/users", "GET")   │
│                                                    │
│         ┌──────────┐          ┌────────────┐      │
│         │  Model   │          │   Adapter   │      │
│         │ (model   │   ←→     │  (policy    │      │
│         │  .conf)  │          │   storage)  │      │
│         └──────────┘          └────────────┘      │
└──────────────────────────────────────────────────┘
```

- **Enforcer（执行器）** — Casbin 的核心 API，你调用 `enforce(sub, obj, act)` 来判断权限
- **Model（模型）** — 定义了权限判断的"规则语言"，如 RBAC 模型、ACL 模型
- **Adapter（适配器）** — 策略数据的存储层，可以是文件、JDBC、Redis、etcd 等

### Model 配置文件详解

Model 是 Casbin 最核心的概念。它定义了权限判断的五个组成部分：

```
[request_definition]    →  请求格式：访问者、资源、操作
[policy_definition]     →  策略格式：角色、资源、操作
[role_definition]       →  角色继承关系
[policy_effect]         →  策略匹配效果：允许优先/拒绝优先
[matchers]              →  匹配规则：判断请求是否匹配策略
```

**RBAC 模型示例（最常用）：**

```ini
[request_definition]
r = sub, obj, act
# 一个请求由三个字段组成：主体(subject)、对象(object)、操作(action)

[policy_definition]
p = sub, obj, act
# 一条策略也由三个字段组成：谁、什么、做什么

[role_definition]
g = _, _
# 定义角色继承关系：g(alice, admin) 表示 alice 拥有 admin 角色

[policy_effect]
e = some(where (p.eft == allow))
# 匹配效果：只要有一条策略允许，结果就是允许

[matchers]
m = g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act
# 匹配规则：请求者的角色等于策略中的角色 且 资源匹配 且 操作匹配
```

**RBAC with Domain（多租户 RBAC）模型：**

```ini
[request_definition]
r = sub, dom, obj, act

[policy_definition]
p = sub, dom, obj, act

[role_definition]
g = _, _, _
# g(alice, admin, tenant1) 表示 alice 在 tenant1 域下是 admin

[matchers]
m = g(r.sub, p.sub, r.dom) && r.dom == p.dom && r.obj == p.obj && r.act == p.act
```

**RBAC with Resource Roles（带资源角色的 RBAC）：**

当权限判断不止依赖用户角色，还依赖资源的属性时：

```ini
[request_definition]
r = sub, obj, act

[policy_definition]
p = sub, obj, act

[role_definition]
g = _, _
g2 = _, _
# g2 定义资源角色关系

[matchers]
m = g(r.sub, p.sub) && g2(r.obj, p.obj) && r.act == p.act
```

### Policy 存储与 Adapter

策略数据可以存储在不同介质中，切换 Adapter 即可：

| Adapter | 存储介质 | 适用场景 |
|---------|---------|---------|
| File Adapter（默认） | `policy.csv` 文件 | 开发环境、简单应用 |
| JDBC Adapter | MySQL、PostgreSQL | 生产环境，需要动态管理 |
| Redis Adapter | Redis | 高并发读取、分布式环境 |
| R2DBC Adapter | 响应式数据库 | WebFlux 项目 |

**Spring Boot 中使用 JDBC Adapter：**

```java
@Bean
public Enforcer enforcer() throws Exception {
    // JDBC Adapter 自动从数据库加载策略
    JdbcAdapter adapter = new JdbcAdapter(dataSource);
    return new Enforcer("model/rbac_model.conf", adapter);
}
```

使用 JDBC Adapter 后，策略存储在数据库表中：

```sql
-- Casbin 策略表结构
CREATE TABLE casbin_rule (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    ptype VARCHAR(255) NOT NULL,  -- p(策略) 或 g(角色)
    v0    VARCHAR(255),           -- subject
    v1    VARCHAR(255),           -- object 或 role
    v2    VARCHAR(255),           -- action
    v3    VARCHAR(255),           -- domain (多租户时用)
    v4    VARCHAR(255),
    v5    VARCHAR(255)
);
```

### 与 Spring Security 集成

在 Spring Boot 中，Casbin 有两种集成方式：

**方式一：独立使用（不与 Spring Security 耦合）**

适合权限逻辑独立、不需要 Spring Security 认证体系的场景：

```java
@Service
public class PermissionService {
    private final Enforcer enforcer;

    public boolean hasPermission(String user, String resource, String action) {
        return enforcer.enforce(user, resource, action);
    }
}
```

**方式二：作为 Spring Security 的权限判断组件（推荐）**

让 Casbin 作为 Spring Security 授权决策的一部分，认证仍由 Spring Security 处理：

```java
@Component
public class CasbinAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private final Enforcer enforcer;

    @Override
    public AuthorizationDecision check(
            Supplier<Authentication> authentication,
            RequestAuthorizationContext context) {
        // 获取当前登录用户
        String user = authentication.get().getName();
        // 获取请求路径和方法
        String path = context.getRequest().getRequestURI();
        String method = context.getRequest().getMethod();

        boolean allowed = enforcer.enforce(user, path, method);
        return new AuthorizationDecision(allowed);
    }
}
```

```java
@Bean
public SecurityFilterChain filterChain(
        HttpSecurity http,
        CasbinAuthorizationManager casbinManager) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth
            .anyRequest().access(casbinManager))  // 所有请求走 Casbin 决策
        .build();
}
```

这种方式的优势：认证（登录、JWT 解析）仍然由 Spring Security 负责，权限判断交给 Casbin，职责清晰。

### Casbin 的操作 API

除了最常用的 `enforce()` 判断权限，Casbin 还提供了策略管理的 API：

```java
// 权限判断
enforcer.enforce("alice", "/api/users", "GET");

// 添加策略
enforcer.addPolicy("admin", "/api/orders", "POST");

// 删除策略
enforcer.removePolicy("user", "/api/users", "DELETE");

// 添加角色
enforcer.addGroupingPolicy("alice", "admin");

// 查询权限：获取 alice 的所有权限
List<List<String>> permissions = enforcer.getImplicitPermissionsForUser("alice");

// 查询角色：获取 admin 角色的所有成员
List<String> users = enforcer.getUsersForRole("admin");
```

### 常见权限模型对比

| 模型 | 定义 | Casbin 支持 | 适用场景 |
|------|------|-----------|---------|
| ACL | 直接给用户分配权限 | `p, alice, data1, read` | 用户少、权限粒度粗 |
| RBAC | 通过角色给用户分配权限 | `g, alice, admin` + `p, admin, data1, read` | 大多数企业应用 |
| RBAC with Domain | 租户域内 RBAC | `g, alice, admin, tenant1` | SaaS 多租户 |
| ABAC | 基于属性的访问控制 | matchers 中使用 `r.sub.Age > 18` | 需要推断型权限判断 |
| RESTful | URL 路径 + HTTP 方法 | `p, admin, /api/users, GET` | REST API 网关 |

Casbin 的强大在于：它不是一个固定的模型，而是一个"权限模型的定义语言"。你可以根据自己的业务需求，用 Model 配置文件定义出任何权限模型。

## 核心要点

1. **Model + Policy 分离是 Casbin 的核心设计理念：** Model 定义"怎么判断权限"的规则结构，Policy 存储"谁有什么权限"的具体数据。两者独立演进，互不影响。
2. **Casbin 解决的是授权问题，不是认证问题：** 它负责回答"你能不能做这个操作"，不负责回答"你是谁"。认证仍然交给 Spring Security。
3. **策略存储通过 Adapter 切换：** 开发环境用 File Adapter，生产环境用 JDBC Adapter 或 Redis Adapter，切换时只需要换一个 Bean。
4. **Model 文件是一次定义、长期稳定的：** 企业的权限模型通常不会频繁变化。变化的是 Policy 数据，可以通过管理界面动态修改。
5. **`enforce()` 是唯一的 API 入口：** 不管你用 RBAC、ABAC 还是自定义模型，判断权限只调一个方法 — 这就是面向接口编程的优势。

## 常见误区

- **把 Casbin 当成认证库来用。** Casbin 不处理登录、不生成 Token、不管理 Session。它的职责边界非常清晰：只做授权决策。认证和授权是两个独立的问题，Casbin 只解决后者。
- **把 Model 文件和 Policy 数据混为一谈。** Model 文件（`model.conf`）定义了规则的语言结构，Policy 数据（数据库中的记录）是具体的权限实例。Model 通常不需要改，Policy 数据可以随时增删改。很多人误以为改了 Policy 需要同时改 Model。
- **用 Casbin 替代 Spring Security 的全部功能。** 建议的架构是：Spring Security 处理 Filter Chain、登录认证、JWT 验证、CSRF 防护；Casbin 只接管授权决策。两者协作，不是替代关系。
- **忽略了 Casbin 的 RoleManager 缓存。** 默认的 RoleManager 每次调用 `enforce()` 都会从 Adapter 重新加载角色继承关系。生产环境中应该配置 CachedRoleManager，避免每次都查询数据库。
- **在 Model 中用 `keyMatch` 时没理解路径匹配规则。** `keyMatch("/api/users/:id", "/api/users/123")` 返回 true，但 `keyMatch("/api/users/:id/orders", "/api/users/123")` 返回 false — 路径匹配函数对格式有严格要求，详细规则需要查阅 Casbin 文档。

## 与其他概念的关联

- **前置：** [Java Spring Security](Java Spring Security.md) -- Casbin 通常与 Spring Security 配合使用，前者负责授权，后者负责认证和过滤器链
- **前置：** [Java Spring JWT](Java Spring JWT.md) -- JWT 承载用户身份信息，Casbin 用这个身份信息做 `enforce()` 判断
- **前置：** [Java Spring 多数据库](./28_Java%20Spring%20多数据库.md) -- Casbin 的策略数据可以存储在关系型数据库、Redis 等多种介质中
- **并行：** [Java Spring AOP](./08_Java%20Spring%20AOP.md) -- 可以把 Casbin 的权限判断封装成自定义注解（如 `@RequirePermission`），通过 AOP 织入到方法调用前
- **后续：** [Java Spring Cloud Gateway](../Spring_Cloud/Java Spring Cloud Gateway.md) -- 在 API 网关层用 Casbin 做统一的入口权限控制
