---
title: Java Spring Controller
created: 2026-05-05 12:16:00
category: Java-Spring
tags:
  - Java
  - Spring
  - Spring-MVC
  - Controller
  - REST
---

<!-- markdownlint-disable MD025 -->

# Java Spring Controller

## 为什么要学 Controller

上一阶段（Part 3）让 Spring Boot 项目跑起来了 -- 有了自动配置、Starter、项目结构、配置文件、可观测性。应用能启动，Actuator 能暴露健康状态。但到目前为止，它还不能处理任何业务请求。

接下来自然要问：**怎么让应用接收 HTTP 请求并返回响应？**

这就是 Controller 的职责。它是 Spring MVC 中处理 HTTP 请求的入口层，负责把外部请求映射到具体的处理方法上。理解 Controller 是写出第一个 API 的起点。

---

## 核心概念

### Controller 是什么

**Controller 是 Spring MVC 中负责接收 HTTP 请求、调用业务逻辑、返回响应的组件。** 用 `@RestController` 标注一个类，它就成为一个 Controller；用 `@GetMapping`、`@PostMapping` 等标注方法，就定义了路由规则。

类比：Controller 就像餐厅的服务员。客人（HTTP 请求）进门后，服务员负责接待、记录点单（解析参数）、把订单传给厨房（调用 Service）、最后把菜端给客人（返回响应）。服务员不做菜，Controller 也不写业务逻辑。

### 为什么需要 Controller

Web 应用需要一个明确的"入口层"来处理 HTTP 协议的细节 -- URL 路径匹配、HTTP 方法区分、请求参数解析、响应格式化。如果把这些逻辑散落在业务代码中，代码会变得混乱且难以维护。Controller 把"HTTP 协议处理"和"业务逻辑"分离开来，让每一层各司其职。

### 没有 Controller 会怎样

没有 Controller 层，你需要直接操作 Servlet API -- 手动解析 URL、手动读取请求体、手动设置响应头和状态码。每个接口都要重复这些样板代码。有了 Controller，Spring MVC 帮你处理所有 HTTP 协议细节，你只需要关注"接收什么参数、返回什么数据"。

---

## 概念深入解释

### DispatcherServlet -- 请求分发的核心

Spring MVC 的所有请求都经过一个中央调度器 -- DispatcherServlet。它是整个请求处理流程的入口。

```
HTTP 请求
    │
    ▼
┌──────────────────┐
│ DispatcherServlet │  ← 中央调度器，所有请求的入口
└──────────────────┘
    │
    ▼
┌──────────────────┐
│  HandlerMapping  │  ← 根据 URL + HTTP 方法找到对应的 Controller 方法
└──────────────────┘
    │
    ▼
┌──────────────────┐
│ HandlerAdapter   │  ← 调用 Controller 方法，处理参数绑定
└──────────────────┘
    │
    ▼
┌──────────────────┐
│ Controller 方法   │  ← 你写的业务入口
└──────────────────┘
    │
    ▼
┌──────────────────┐
│ MessageConverter │  ← 把返回值序列化为 JSON（或其他格式）
└──────────────────┘
    │
    ▼
HTTP 响应
```

你不需要手动配置 DispatcherServlet -- Spring Boot 自动配置已经帮你做了。但理解这个流程有助于排查"请求为什么没到我的方法"类问题。

### @Controller vs @RestController

| 注解 | 行为 | 适用场景 |
|------|------|----------|
| `@Controller` | 方法返回值默认解析为视图名（模板引擎） | 服务端渲染（Thymeleaf、JSP） |
| `@RestController` | 方法返回值直接序列化为响应体（JSON） | RESTful API（前后端分离） |

`@RestController` = `@Controller` + `@ResponseBody`。在前后端分离架构中，几乎总是使用 `@RestController`。

### 路由映射注解

```java
@RestController
@RequestMapping("/api/users")  // 类级别：公共前缀
public class UserController {

    @GetMapping           // GET /api/users
    public List<User> list() { ... }

    @GetMapping("/{id}")  // GET /api/users/123
    public User getById(@PathVariable Long id) { ... }

    @PostMapping          // POST /api/users
    public User create(@RequestBody UserDTO dto) { ... }

    @PutMapping("/{id}")  // PUT /api/users/123
    public User update(@PathVariable Long id, @RequestBody UserDTO dto) { ... }

    @DeleteMapping("/{id}")  // DELETE /api/users/123
    public void delete(@PathVariable Long id) { ... }
}
```

路由映射遵循 RESTful 风格：

| HTTP 方法 | 语义 | 对应注解 |
|-----------|------|----------|
| GET | 查询资源 | `@GetMapping` |
| POST | 创建资源 | `@PostMapping` |
| PUT | 全量更新 | `@PutMapping` |
| PATCH | 部分更新 | `@PatchMapping` |
| DELETE | 删除资源 | `@DeleteMapping` |

### Controller 的职责边界

Controller 应该做的事：

- 接收和解析请求参数
- 调用 Service 层方法
- 返回响应（包括状态码）

Controller 不应该做的事：

- 直接操作数据库（那是 Repository 的事）
- 包含业务逻辑（那是 Service 的事）
- 处理事务（那是 Service 层用 `@Transactional` 的事）

一个 Controller 方法理想情况下只有 3-5 行：接收参数 → 调用 Service → 返回结果。

### ResponseEntity -- 精细控制响应

当你需要控制 HTTP 状态码或响应头时，使用 `ResponseEntity`：

```java
@PostMapping
public ResponseEntity<User> create(@RequestBody UserDTO dto) {
    User user = userService.create(dto);
    return ResponseEntity
        .status(HttpStatus.CREATED)  // 201
        .header("X-Custom-Header", "value")
        .body(user);
}
```

如果不需要精细控制，直接返回对象即可（默认 200 OK）。

---

## 核心要点

1. **Controller 只做"接收-转发-返回"。** 业务逻辑放 Service，数据操作放 Repository，Controller 是薄薄的一层胶水。
2. **用 `@RestController` 而非 `@Controller`。** 前后端分离架构下，返回值直接序列化为 JSON，不需要视图解析。
3. **类级别 `@RequestMapping` 定义公共前缀。** 避免每个方法重复写 `/api/users`。
4. **遵循 RESTful 语义选择 HTTP 方法。** GET 查询、POST 创建、PUT 更新、DELETE 删除 -- 这不是强制的，但是行业共识。
5. **需要控制状态码时用 `ResponseEntity`。** 创建资源返回 201，删除返回 204，这些语义比统一返回 200 更规范。

---

## 常见误区

- **在 Controller 中写业务逻辑。** Controller 方法超过 10 行通常意味着职责越界。业务判断、数据转换、事务管理都应该下沉到 Service 层。Controller 膨胀会导致难以测试和复用。
- **`@RequestMapping` 放在方法上却不指定 HTTP 方法。** `@RequestMapping("/users")` 不指定 `method` 时会匹配所有 HTTP 方法（GET、POST、DELETE 都能访问），这是安全隐患。应该用具体的 `@GetMapping`、`@PostMapping` 等。
- **Controller 直接注入 Repository。** 跳过 Service 层看似简洁，但当业务变复杂（需要事务、需要组合多个 Repository 操作）时，你会被迫重构。从一开始就保持 Controller → Service → Repository 的分层。
- **忘记 `@RequestMapping` 的路径以 `/` 开头。** 路径不以 `/` 开头时，Spring 会尝试拼接，可能产生意外的路由。始终使用绝对路径 `/api/users` 而非 `api/users`。

---

## 与其他概念的关联

- **前置：** [Java Spring Boot 项目结构](./13_Java%20Spring%20Boot%20项目结构.md) -- Controller 类的存放位置遵循项目结构约定。[Java Spring IoC](./05_Java%20Spring%20IoC.md) -- Controller 本身是一个 Bean，由容器管理。[Java Spring DI](./07_Java%20Spring%20DI.md) -- Controller 通过构造器注入 Service 依赖。
- **并行：** [Java Spring 请求处理](./17_Java%20Spring%20请求处理.md) -- Controller 方法的参数绑定细节。[Java Spring 响应与 DTO](./18_Java%20Spring%20响应与%20DTO.md) -- Controller 方法的返回值设计。
- **后续：** [Java Spring 异常处理](./19_Java%20Spring%20异常处理.md) -- Controller 抛出的异常如何统一处理。[Java Spring 参数校验](./20_Java%20Spring%20参数校验.md) -- 请求参数的自动校验机制。[Java Spring OpenAPI](./21_Java%20Spring%20OpenAPI.md) -- 基于 Controller 注解自动生成 API 文档。
