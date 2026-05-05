---
title: Java Spring 异常处理
created: 2026-05-05 12:16:00
category: Java-Spring
tags:
  - Java
  - Spring
  - Spring-MVC
  - Exception-Handling
  - ControllerAdvice
---

<!-- markdownlint-disable MD025 -->

# Java Spring 异常处理

## 为什么要学异常处理

前面讲了 Controller、请求处理、响应与 DTO -- 正常流程已经打通了。但现实中，请求不总是成功的：参数格式错误、资源不存在、业务规则不满足、下游服务超时... 这些异常情况如果不统一处理，客户端会收到 Spring 默认的错误页面或者不可读的堆栈信息。

全局异常处理解决的问题是：**无论哪个 Controller 抛出什么异常，客户端都能收到格式统一、信息清晰的错误响应。**

---

## 核心概念

### 全局异常处理是什么

**全局异常处理是通过 `@ControllerAdvice` + `@ExceptionHandler` 实现的统一异常拦截机制。** 它在 Controller 方法抛出异常时自动介入，将异常转换为结构化的错误响应返回给客户端。

类比：就像公司的客服部门。无论哪个部门（Controller）出了问题，客户（客户端）都不会直接看到内部的混乱（堆栈信息），而是收到客服（异常处理器）整理好的、格式统一的回复。

### 为什么需要全局异常处理

如果在每个 Controller 方法里都写 try-catch，代码会变得冗长且不一致。不同开发者可能返回不同格式的错误信息，前端无法用统一逻辑处理。全局异常处理把"异常 → 错误响应"的转换逻辑集中在一个地方，保证所有接口的错误格式一致。

### 没有全局异常处理会怎样

Spring Boot 默认的错误处理会返回一个 `WhitelabelErrorPage`（浏览器）或者一个包含 `timestamp`、`status`、`error`、`path` 的 JSON（API 调用）。这个默认格式通常不符合项目需求 -- 缺少业务错误码、错误信息不够友好、可能泄露内部实现细节。有了全局异常处理，你可以完全控制错误响应的格式和内容。

---

## 概念深入解释

### @ControllerAdvice 机制

`@ControllerAdvice` 标注的类会被 Spring 自动扫描，其中的 `@ExceptionHandler` 方法会拦截所有 Controller 抛出的对应异常：

```
Controller 方法抛出异常
    │
    ▼
DispatcherServlet 捕获异常
    │
    ▼
查找匹配的 @ExceptionHandler 方法
    │
    ├── 先找 Controller 内部的 @ExceptionHandler
    └── 再找 @ControllerAdvice 中的 @ExceptionHandler
    │
    ▼
执行 Handler 方法，返回错误响应
```

### 基本实现结构

```java
@RestControllerAdvice  // = @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleNotFound(ResourceNotFoundException e) {
        return ApiResponse.error(404, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleBusiness(BusinessException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleUnknown(Exception e) {
        log.error("Unexpected error", e);
        return ApiResponse.error(500, "服务器内部错误");
    }
}
```

### 自定义业务异常

定义业务异常类，携带业务错误码：

```java
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() { return code; }
}

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Object id) {
        super(404, resource + " not found: " + id);
    }
}
```

Service 层抛出业务异常，全局处理器统一转换：

```java
// Service 中
public User getById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", id));
}
```

### 异常处理的优先级

当多个 `@ExceptionHandler` 都能匹配时，Spring 选择最具体的那个：

1. 精确匹配异常类型（如 `ResourceNotFoundException.class`）
2. 父类匹配（如 `BusinessException.class` 能捕获所有业务异常子类）
3. 兜底匹配（`Exception.class` 捕获所有未被前面处理的异常）

建议按从具体到通用的顺序定义 Handler。

### Spring MVC 内置异常

Spring MVC 在参数绑定、校验等环节会抛出一些内置异常，需要在全局处理器中覆盖：

| 异常类 | 触发场景 | 建议 HTTP 状态码 |
|--------|----------|-----------------|
| `MethodArgumentNotValidException` | `@Valid` 校验失败 | 400 |
| `MissingServletRequestParameterException` | 必填参数缺失 | 400 |
| `HttpMessageNotReadableException` | 请求体 JSON 格式错误 | 400 |
| `HttpRequestMethodNotSupportedException` | HTTP 方法不支持 | 405 |
| `NoHandlerFoundException` | 路由不存在 | 404 |
| `MethodArgumentTypeMismatchException` | 参数类型转换失败 | 400 |

### 错误响应设计

一个好的错误响应应该包含：

```json
{
  "code": 40001,
  "message": "用户名已存在",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/users",
  "details": [
    { "field": "username", "message": "长度必须在 3-20 之间" }
  ]
}
```

- `code` -- 业务错误码（比 HTTP 状态码更细粒度）
- `message` -- 面向用户的错误描述
- `timestamp` -- 错误发生时间（便于排查）
- `path` -- 请求路径
- `details` -- 字段级错误详情（校验失败时）

**注意：** 生产环境不要在错误响应中暴露堆栈信息或内部实现细节。

---

## 核心要点

1. **用 `@RestControllerAdvice` 集中处理所有异常。** 不要在每个 Controller 方法里写 try-catch，那是分散的、不一致的。
2. **定义业务异常体系。** 基类 `BusinessException` 携带错误码，子类表达具体业务错误（`ResourceNotFoundException`、`DuplicateException` 等）。
3. **兜底 `Exception.class` 必须有。** 防止未预料的异常直接暴露堆栈信息给客户端。兜底处理器应该记录日志并返回通用错误信息。
4. **覆盖 Spring MVC 内置异常。** `MethodArgumentNotValidException`、`HttpMessageNotReadableException` 等需要转换为你的统一错误格式。
5. **生产环境隐藏内部细节。** 错误响应只包含业务信息，堆栈和内部异常信息只写入日志。

---

## 常见误区

- **在 Controller 中 catch 异常后返回 null 或空对象。** 这会让客户端无法区分"查询成功但数据为空"和"发生了错误"。应该让异常向上抛出，由全局处理器转换为明确的错误响应。
- **所有异常都返回 HTTP 500。** 参数错误应该是 400，资源不存在应该是 404，权限不足应该是 403。正确的 HTTP 状态码让客户端和监控系统能正确分类错误。
- **`@ExceptionHandler` 方法中又抛出异常。** 如果异常处理器本身抛出异常，Spring 会回退到默认错误处理，客户端收到不可控的响应。处理器方法内部应该用 try-catch 保护。
- **忘记处理 `MethodArgumentNotValidException`。** 参数校验失败时 Spring 抛出这个异常，默认返回的错误格式通常不符合项目规范。需要在全局处理器中提取字段错误信息，转换为统一格式。
- **在错误响应中暴露 SQL 语句或堆栈信息。** `DataAccessException` 的 message 可能包含 SQL 片段，直接返回给客户端是安全隐患。应该只返回通用的"数据库操作失败"信息，详细错误写入日志。

---

## 与其他概念的关联

- **前置：** [Java Spring Controller](./16_Java%20Spring%20Controller.md) -- 异常从 Controller 方法中抛出。[Java Spring 响应与 DTO](./18_Java%20Spring%20响应与%20DTO.md) -- 错误响应也需要遵循统一的 `ApiResponse` 格式。
- **并行：** [Java Spring 参数校验](./20_Java%20Spring%20参数校验.md) -- 校验失败会抛出 `MethodArgumentNotValidException`，需要在全局异常处理器中捕获和格式化。
- **后续：** [Java Spring Security](./26_Java%20Spring%20Security.md) -- 安全相关的异常（认证失败、权限不足）有独立的处理机制（`AuthenticationEntryPoint`、`AccessDeniedHandler`），与 `@ControllerAdvice` 互补。
