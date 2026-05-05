---
title: Java Spring 请求处理
created: 2026-05-05 12:16:00
category: Java-Spring
tags:
  - Java
  - Spring
  - Spring-MVC
  - Request
  - Parameter-Binding
---

<!-- markdownlint-disable MD025 -->

# Java Spring 请求处理

## 为什么要学请求处理

上一节讲了 Controller 的定位 -- 它是 HTTP 请求的入口，负责接收请求、调用 Service、返回响应。但"接收请求"这件事本身有很多细节：路径里的参数怎么取？查询字符串怎么绑定？JSON 请求体怎么变成 Java 对象？请求头怎么读？

这些就是请求参数处理要解决的问题。Spring MVC 提供了一套注解驱动的参数绑定机制，让你用声明式的方式告诉框架"我要什么"，框架负责"怎么给你"。

---

## 核心概念

### 请求参数绑定是什么

**请求参数绑定（Parameter Binding）是 Spring MVC 自动将 HTTP 请求中的数据（路径、查询参数、请求体、请求头等）转换为 Controller 方法参数的机制。** 你只需要在方法参数上加注解声明数据来源，Spring 自动完成提取和类型转换。

类比：就像快递分拣。一个包裹（HTTP 请求）上有收件地址（URL）、包裹单号（路径参数）、备注信息（查询参数）、包裹内容（请求体）。分拣系统（Spring MVC）根据标签自动把每部分信息送到对应的位置（方法参数）。

### 为什么需要请求参数绑定

HTTP 请求的数据分散在不同位置 -- URL 路径、查询字符串、请求体、请求头、Cookie。手动从 `HttpServletRequest` 中逐个提取、做类型转换、处理缺失值，代码会非常冗长且容易出错。参数绑定机制把这些重复工作自动化了。

### 没有请求参数绑定会怎样

你需要手动调用 `request.getParameter("name")`、手动做 `Integer.parseInt()`、手动处理 `null` 值、手动解析 JSON 请求体。每个 Controller 方法都要写一堆样板代码。有了参数绑定，一个 `@PathVariable Long id` 就搞定了路径参数的提取和类型转换。

---

## 概念深入解释

### 参数来源与对应注解

| 数据来源 | 注解 | 示例 URL / 请求 | 方法参数 |
|----------|------|-----------------|----------|
| URL 路径 | `@PathVariable` | `/users/123` | `@PathVariable Long id` |
| 查询参数 | `@RequestParam` | `/users?name=tom&age=20` | `@RequestParam String name` |
| 请求体 | `@RequestBody` | POST body: `{"name":"tom"}` | `@RequestBody UserDTO dto` |
| 请求头 | `@RequestHeader` | `Authorization: Bearer xxx` | `@RequestHeader String authorization` |
| Cookie | `@CookieValue` | `Cookie: session=abc` | `@CookieValue String session` |

### @PathVariable -- 路径参数

从 URL 路径模板中提取变量：

```java
@GetMapping("/users/{id}/orders/{orderId}")
public Order getOrder(
    @PathVariable Long id,
    @PathVariable Long orderId) {
    // id = 123, orderId = 456
    // 当 URL 为 /users/123/orders/456
}
```

变量名默认与方法参数名匹配。如果不一致，需要显式指定：`@PathVariable("id") Long userId`。

### @RequestParam -- 查询参数

从查询字符串中提取参数：

```java
@GetMapping("/users")
public List<User> search(
    @RequestParam String keyword,
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(required = false) String sort) {
    // GET /users?keyword=tom&page=2
}
```

关键属性：

- `required` -- 默认 `true`，参数缺失时返回 400
- `defaultValue` -- 设置默认值，设置后 `required` 自动变为 `false`
- `name` -- 当参数名与方法参数名不一致时使用

### @RequestBody -- 请求体

将 JSON 请求体反序列化为 Java 对象：

```java
@PostMapping("/users")
public User create(@RequestBody CreateUserDTO dto) {
    // Spring 使用 Jackson 将 JSON 自动转为 CreateUserDTO 对象
}
```

底层由 `HttpMessageConverter`（默认是 Jackson 的 `MappingJackson2HttpMessageConverter`）完成 JSON ↔ Java 对象的转换。

**注意：** 一个方法只能有一个 `@RequestBody` 参数，因为请求体只能被读取一次。

### @RequestHeader -- 请求头

```java
@GetMapping("/users")
public List<User> list(
    @RequestHeader("X-Request-Id") String requestId,
    @RequestHeader(value = "Accept-Language", defaultValue = "zh-CN") String lang) {
}
```

### 无注解参数 -- POJO 绑定

当查询参数很多时，可以直接用一个 POJO 接收，不需要加任何注解：

```java
// GET /users?name=tom&age=20&page=1&size=10
@GetMapping("/users")
public List<User> search(UserSearchCriteria criteria) {
    // Spring 自动将查询参数绑定到 POJO 的同名字段
}
```

这种方式适合参数较多的查询接口，避免方法签名过长。

### 类型转换机制

Spring 内置了常见类型的转换器：

| 源类型（String） | 目标类型 | 说明 |
|------------------|----------|------|
| `"123"` | `Long` / `Integer` | 数字转换 |
| `"true"` | `Boolean` | 布尔转换 |
| `"2024-01-15"` | `LocalDate` | 日期转换（需配置格式） |
| `"ACTIVE"` | `enum Status` | 枚举转换（按名称匹配） |

日期类型需要额外配置格式：

```java
@GetMapping("/orders")
public List<Order> search(
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
}
```

或者在 `application.yml` 中全局配置 Jackson 的日期格式。

### 请求处理流程

```
HTTP 请求到达
    │
    ▼
HandlerAdapter 识别目标 Controller 方法
    │
    ▼
遍历方法参数，逐个解析：
    ├── 有 @PathVariable？→ 从 URL 模板提取
    ├── 有 @RequestParam？→ 从查询字符串提取
    ├── 有 @RequestBody？ → 用 MessageConverter 反序列化
    ├── 有 @RequestHeader？→ 从请求头提取
    └── 无注解 POJO？    → 从查询参数逐字段绑定
    │
    ▼
类型转换（String → 目标类型）
    │
    ▼
调用 Controller 方法
```

---

## 核心要点

1. **一个注解对应一个数据来源。** `@PathVariable` 取路径、`@RequestParam` 取查询参数、`@RequestBody` 取请求体 -- 不要混淆。
2. **`@RequestBody` 只能用一次。** HTTP 请求体是流式读取的，只能消费一次。如果需要多个对象，把它们包装在一个外层 DTO 中。
3. **查询参数多时用 POJO 绑定。** 超过 3 个 `@RequestParam` 就应该考虑用一个查询对象替代，方法签名更清晰。
4. **善用 `defaultValue` 和 `required = false`。** 对可选参数提供合理默认值，避免客户端必须传递所有参数。
5. **日期类型需要显式指定格式。** Spring 不会猜测日期格式，不配置会导致 400 错误。

---

## 常见误区

- **GET 请求用 `@RequestBody` 接收参数。** GET 请求按 HTTP 规范不应该有请求体（虽然技术上可以）。GET 的参数应该放在 URL 路径或查询字符串中，用 `@PathVariable` 或 `@RequestParam`。
- **`@RequestParam` 和 `@PathVariable` 搞混。** `/users?id=123` 用 `@RequestParam`，`/users/123` 用 `@PathVariable`。前者是查询参数，后者是路径的一部分。
- **忘记 `@RequestBody` 导致 JSON 参数全为 null。** POST 请求发送 JSON 但方法参数没加 `@RequestBody`，Spring 会尝试从查询参数绑定，结果所有字段都是 null。这是新手最常见的 400/空值问题。
- **路径变量名与方法参数名不一致却不显式指定。** `@GetMapping("/{userId}")` 配 `@PathVariable Long id` 会报错。要么保持名称一致，要么用 `@PathVariable("userId") Long id`。
- **POJO 绑定时字段没有 setter。** 无注解的 POJO 参数绑定依赖 setter 方法（或公开字段）。如果用了 Lombok 的 `@Value`（不可变对象），绑定会失败。应该用 `@Data` 或手动提供 setter。

---

## 与其他概念的关联

- **前置：** [Java Spring Controller](./16_Java%20Spring%20Controller.md) -- Controller 定义了路由，请求处理是 Controller 方法参数的具体绑定机制。[Java 注解机制](./02_Java%20注解机制.md) -- 参数绑定注解的工作原理依赖 Java 注解和反射。
- **并行：** [Java Spring 响应与 DTO](./18_Java%20Spring%20响应与%20DTO.md) -- 请求处理关注"入"，响应与 DTO 关注"出"，两者共同构成 Controller 方法的完整签名。
- **后续：** [Java Spring 参数校验](./20_Java%20Spring%20参数校验.md) -- 参数绑定完成后，下一步是校验参数的合法性。[Java Spring 异常处理](./19_Java%20Spring%20异常处理.md) -- 参数绑定失败（类型不匹配、必填缺失）会抛出异常，需要全局异常处理器捕获。
