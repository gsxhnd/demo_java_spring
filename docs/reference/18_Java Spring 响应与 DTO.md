---
title: Java Spring 响应与 DTO
created: 2026-05-05 12:16:00
category: Java-Spring
tags:
  - Java
  - Spring
  - Spring-MVC
  - DTO
  - Response
---

<!-- markdownlint-disable MD025 -->

# Java Spring 响应与 DTO

## 为什么要学响应与 DTO

上一节讲了请求参数处理 -- 如何把 HTTP 请求中的数据绑定到 Controller 方法参数。那是"入"的部分。现在要解决"出"的问题：**Controller 方法应该返回什么？怎么返回？**

直觉上，直接把数据库查出来的 Entity 对象返回给客户端似乎最简单。但这样做会带来一系列问题 -- 暴露内部字段（密码、软删除标记）、API 和数据库结构耦合、无法独立演进接口格式。DTO（Data Transfer Object）就是为了解决这些问题而存在的。

---

## 核心概念

### DTO 是什么

**DTO（Data Transfer Object）是专门用于 API 层数据传输的对象。** 它不是数据库实体，不包含业务逻辑，只定义"这个接口需要接收/返回哪些字段"。

类比：DTO 就像餐厅的菜单。厨房里有完整的食材清单（Entity），但客人看到的只是菜单上精心挑选的菜品描述（DTO）。你不会把仓库库存表直接给客人看。

### 为什么需要 DTO

Entity 是面向数据库的，DTO 是面向客户端的。两者的关注点不同：

- Entity 可能有 `password`、`deletedAt`、`version` 等内部字段，不应该暴露给客户端
- 一个 API 可能需要组合多个 Entity 的数据
- API 的字段命名可能和数据库不同（如数据库用 `created_at`，API 返回 `createdAt`）
- API 版本升级时，DTO 可以独立变化而不影响数据库结构

### 没有 DTO 会怎样

直接返回 Entity 会导致：API 和数据库表结构强耦合（改表就改 API）、敏感字段泄露（密码、内部 ID）、无法为不同场景定制返回格式（列表页和详情页需要的字段不同）、Jackson 序列化时可能触发懒加载导致 N+1 查询。有了 DTO，API 层和数据层彻底解耦，各自独立演进。

---

## 概念深入解释

### DTO 的分类

在实际项目中，DTO 通常按用途分为几类：

| 类型 | 命名约定 | 用途 | 示例 |
|------|----------|------|------|
| Request DTO | `CreateXxxDTO` / `XxxRequest` | 接收客户端请求数据 | `CreateUserDTO` |
| Response DTO | `XxxVO` / `XxxResponse` | 返回给客户端的数据 | `UserVO` |
| Query DTO | `XxxQuery` / `XxxCriteria` | 封装查询条件 | `UserSearchQuery` |

命名没有强制标准，团队统一即可。常见的约定是：入参用 `DTO`/`Request`，出参用 `VO`/`Response`。

### Entity vs DTO 对比

```java
// Entity -- 面向数据库
@Entity
public class User {
    @Id private Long id;
    private String username;
    private String password;      // 不应暴露
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt; // 软删除标记，不应暴露
    private Integer version;       // 乐观锁，不应暴露
}

// Response DTO -- 面向客户端
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String createdAt;  // 格式化后的字符串
}
```

### 统一响应格式

生产项目通常会定义统一的响应包装结构：

```java
public class ApiResponse<T> {
    private int code;       // 业务状态码
    private String message; // 提示信息
    private T data;         // 实际数据

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static ApiResponse<?> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

使用方式：

```java
@GetMapping("/{id}")
public ApiResponse<UserVO> getUser(@PathVariable Long id) {
    UserVO user = userService.getById(id);
    return ApiResponse.success(user);
}
```

统一响应格式的好处：前端可以用统一的逻辑处理所有接口的响应，不需要为每个接口写不同的解析逻辑。

### DTO 转换

Entity 和 DTO 之间需要转换。常见方式：

| 方式 | 优点 | 缺点 |
|------|------|------|
| 手动转换 | 简单直接，无额外依赖 | 字段多时代码冗长 |
| MapStruct | 编译期生成代码，性能好 | 需要额外依赖和注解处理器 |
| BeanUtils.copyProperties | 一行代码 | 运行时反射，字段不匹配时静默失败 |

推荐 MapStruct -- 它在编译期生成转换代码，既避免了手写样板代码，又没有运行时反射的性能开销。

### Jackson 序列化控制

Spring 默认使用 Jackson 将对象序列化为 JSON。常用注解：

```java
public class UserVO {
    private Long id;
    private String username;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonIgnore  // 不序列化此字段
    private String internalNote;

    @JsonProperty("nick")  // 序列化时字段名改为 "nick"
    private String nickname;
}
```

全局配置（`application.yml`）：

```yaml
spring:
  jackson:
    default-property-inclusion: non_null  # null 字段不输出
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
```

### HTTP 状态码语义

| 状态码 | 含义 | 使用场景 |
|--------|------|----------|
| 200 OK | 请求成功 | GET 查询、PUT 更新 |
| 201 Created | 资源已创建 | POST 创建 |
| 204 No Content | 成功但无返回体 | DELETE 删除 |
| 400 Bad Request | 请求参数错误 | 校验失败 |
| 404 Not Found | 资源不存在 | 查询不到 |
| 500 Internal Server Error | 服务器内部错误 | 未捕获异常 |

---

## 核心要点

1. **永远不要直接返回 Entity。** Entity 面向数据库，DTO 面向客户端。两者的生命周期和关注点不同，必须分离。
2. **入参和出参用不同的 DTO。** 创建用户需要密码（`CreateUserDTO`），返回用户不需要密码（`UserVO`）。不要用一个类兼顾两种场景。
3. **统一响应格式是团队协作的基础。** `ApiResponse<T>` 让前端可以用统一逻辑处理所有接口，减少沟通成本。
4. **DTO 转换推荐 MapStruct。** 编译期生成代码，类型安全，字段遗漏在编译时就能发现。
5. **善用 Jackson 注解控制序列化行为。** `@JsonIgnore` 排除敏感字段，`@JsonFormat` 格式化日期，`@JsonProperty` 重命名字段。

---

## 常见误区

- **Controller 直接返回 JPA Entity 导致懒加载异常。** Entity 上的 `@OneToMany(fetch = LAZY)` 关联字段在序列化时会触发数据库查询。如果此时 Session 已关闭，会抛出 `LazyInitializationException`。使用 DTO 可以彻底避免这个问题。
- **一个 DTO 打天下。** 用同一个 `UserDTO` 同时做创建入参和查询出参，导致字段混乱（创建时不需要 `id`，查询时不需要 `password`）。应该按场景拆分：`CreateUserDTO`、`UpdateUserDTO`、`UserVO`。
- **在 DTO 中放业务逻辑。** DTO 应该是纯数据载体（只有字段和 getter/setter），不应该包含计算逻辑或数据库访问。业务逻辑属于 Service 层。
- **`BeanUtils.copyProperties` 字段名不匹配时不报错。** 源对象有 `userName`，目标对象有 `username`，复制时静默跳过，导致字段为 null。MapStruct 在编译期就会警告未映射的字段。
- **统一响应格式中 HTTP 状态码始终返回 200。** 有些团队把错误信息放在 `ApiResponse.code` 中，HTTP 状态码永远是 200。这会导致监控系统无法正确统计错误率。应该同时使用正确的 HTTP 状态码和业务状态码。

---

## 与其他概念的关联

- **前置：** [Java Spring Controller](./16_Java%20Spring%20Controller.md) -- DTO 是 Controller 方法的返回值类型。[Java Spring 请求处理](./17_Java%20Spring%20请求处理.md) -- Request DTO 通过 `@RequestBody` 接收。
- **并行：** [Java Spring 异常处理](./19_Java%20Spring%20异常处理.md) -- 错误响应也需要统一格式，与正常响应的 `ApiResponse` 保持一致。
- **后续：** [Java Spring ORM 与 JPA](./22_Java%20Spring%20ORM%20与%20JPA.md) -- Entity 是 DTO 的数据来源，理解两者的区别是数据访问层的前提。[Java Spring 参数校验](./20_Java%20Spring%20参数校验.md) -- 校验注解加在 Request DTO 的字段上。
