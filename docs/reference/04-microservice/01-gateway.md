# Spring Cloud Gateway — API 网关 / API Gateway

> 微服务统一入口：路由转发、过滤器链、限流、CORS、灰度发布

## 1. 概述 / Overview

Spring Cloud Gateway 是 Spring 官方的 API 网关，基于 WebFlux（Netty）构建，天然支持异步非阻塞。它是微服务架构中的统一入口，负责请求路由、鉴权、限流、日志等横切关注点。

### 网关的职责

| 职责 | 说明 |
|---|---|
| 路由转发 (Routing) | 根据路径/Header/参数将请求转发到对应微服务 |
| 负载均衡 (Load Balance) | 结合服务发现，自动负载均衡到多个实例 |
| 鉴权 (Authentication) | 统一 Token 校验，避免每个服务重复实现 |
| 限流 (Rate Limiting) | 保护后端服务，防止流量洪峰 |
| 熔断降级 (Circuit Breaker) | 后端服务不可用时返回降级响应 |
| 日志 / 链路追踪 | 统一记录请求日志，注入 Trace ID |
| CORS 跨域 | 统一处理跨域请求 |
| 灰度发布 | 按权重/Header 路由到不同版本 |

### Gateway vs Nginx vs Zuul

| 特性 | Spring Cloud Gateway | Nginx | Zuul 1.x |
|---|---|---|---|
| 运行时 | Netty（非阻塞） | C（事件驱动） | Servlet（阻塞） |
| 性能 | 高 | 极高 | 中 |
| 动态路由 | 支持（代码/配置中心） | 需 reload | 支持 |
| 服务发现集成 | 原生支持 | 需 lua 插件 | 原生支持 |
| 过滤器扩展 | Java 代码，灵活 | Lua/OpenResty | Java 代码 |
| 适用场景 | Java 微服务网关 | 反向代理/静态资源 | 已过时，不推荐 |

---

## 2. 核心概念 / Core Concepts

### 三大核心组件

```
Client Request
       │
       ▼
┌─────────────────┐
│   Route (路由)   │  ← 定义：ID + URI + Predicate + Filter
│                 │
│  ┌────────────┐ │
│  │ Predicate  │ │  ← 匹配条件：Path/Header/Method/Query/Time...
│  │ (断言)     │ │
│  └─────┬──────┘ │
│        │ match  │
│  ┌─────▼──────┐ │
│  │  Filter    │ │  ← 过滤器：修改请求/响应、鉴权、限流、日志
│  │ (过滤器)   │ │
│  └─────┬──────┘ │
│        │        │
└────────┼────────┘
         ▼
  Upstream Service
```

### Route 路由

路由是网关的基本单元，由以下部分组成：

| 组成 | 说明 |
|---|---|
| ID | 路由唯一标识 |
| URI | 目标服务地址（`http://` 或 `lb://service-name`） |
| Predicate | 匹配条件（何时命中此路由） |
| Filter | 过滤器链（对请求/响应做处理） |

### Predicate 内置断言工厂

| 断言 | 说明 |
|---|---|
| `Path` | 路径匹配：`/api/user/**` |
| `Method` | HTTP 方法：`GET`, `POST` |
| `Header` | 请求头匹配：`X-Request-Id, \d+` |
| `Query` | 查询参数：`name, zhangsan` |
| `Host` | 域名匹配：`**.example.com` |
| `After` / `Before` / `Between` | 时间范围 |
| `Weight` | 权重路由（灰度发布） |
| `RemoteAddr` | 客户端 IP |

### Filter 内置过滤器

| 过滤器 | 说明 |
|---|---|
| `AddRequestHeader` | 添加请求头 |
| `AddResponseHeader` | 添加响应头 |
| `StripPrefix` | 去除路径前缀 |
| `RewritePath` | 路径重写 |
| `RequestRateLimiter` | 限流（基于 Redis） |
| `CircuitBreaker` | 熔断降级 |
| `Retry` | 重试 |
| `SetStatus` | 设置响应状态码 |

### Global Filter vs Gateway Filter

| 类型 | 作用范围 | 配置方式 |
|---|---|---|
| Gateway Filter | 单个路由 | YAML 或 Java 配置 |
| Global Filter | 所有路由 | 实现 `GlobalFilter` 接口 |

---

## 3. 快速集成 / Quick Start

### Maven 依赖

- `spring-cloud-starter-gateway` — 网关核心
- `spring-cloud-starter-loadbalancer` — 负载均衡（配合服务发现）

### 关键配置项

| 配置路径 | 说明 |
|---|---|
| `spring.cloud.gateway.routes[].id` | 路由 ID |
| `spring.cloud.gateway.routes[].uri` | 目标 URI |
| `spring.cloud.gateway.routes[].predicates[]` | 断言列表 |
| `spring.cloud.gateway.routes[].filters[]` | 过滤器列表 |
| `spring.cloud.gateway.default-filters[]` | 全局默认过滤器 |
| `spring.cloud.gateway.globalcors` | 全局 CORS 配置 |
| `spring.cloud.gateway.discovery.locator.enabled` | 自动从注册中心发现路由 |

---

## 4. 进阶要点 / Advanced Topics

- **动态路由**：通过 Nacos / 数据库存储路由配置，运行时动态刷新，无需重启
- **自定义 GlobalFilter**：实现统一鉴权、请求日志、Trace ID 注入
- **自定义 Predicate Factory**：按业务逻辑自定义路由匹配规则
- **限流方案**：`RequestRateLimiter` + Redis（令牌桶算法），按 IP / 用户 / API 限流
- **熔断集成**：Gateway Filter 中集成 Resilience4j CircuitBreaker
- **灰度发布**：`Weight` 断言按权重分流，或自定义 Header 路由到灰度版本
- **WebSocket 代理**：Gateway 原生支持 WebSocket 路由
- **请求体缓存**：`CacheRequestBody` 过滤器，解决请求体只能读一次的问题
- **跨域 CORS**：全局配置 `globalcors` 或自定义 `CorsWebFilter`

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| Gateway 不能用 spring-boot-starter-web | Gateway 基于 WebFlux，不能与 spring-web（Servlet）共存 |
| 路由不生效 | 检查 Predicate 顺序，先匹配先命中 |
| 转发后丢失 Host Header | 设置 `spring.cloud.gateway.x-forwarded.host-enabled=true` |
| 限流不生效 | 确认 Redis 连接正常，KeyResolver Bean 已注册 |
| 大文件上传失败 | 调大 `spring.codec.max-in-memory-size` |
| 服务发现路由 404 | 确认 `lb://service-name` 中的服务名与注册中心一致（大小写） |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-microservice-demo/gateway-service/`](../../examples/spring-microservice-demo/gateway-service/)（待生成）

## 7. 参考链接 / References

- [Spring Cloud Gateway 官方文档](https://docs.spring.io/spring-cloud-gateway/reference/)
- [Spring Cloud Gateway GitHub](https://github.com/spring-cloud/spring-cloud-gateway)
