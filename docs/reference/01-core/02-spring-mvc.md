# Spring MVC Web 开发基础 / Spring MVC Fundamentals

> Spring MVC 是构建 RESTful API 和 Web 应用的核心框架。掌握请求处理、参数校验、异常处理和拦截器，是写好 Web 服务的基本功。

## 1. 概述 / Overview

Spring MVC 基于 Servlet 规范，采用前端控制器模式（Front Controller Pattern）。所有请求经过 `DispatcherServlet` 统一分发，由 Handler（Controller）处理后返回响应。Spring Boot 内嵌 Tomcat，开箱即用。

## 2. 核心概念 / Core Concepts

### 2.1 请求处理流程

```
Client Request
    │
    ▼
DispatcherServlet (前端控制器)
    │
    ├──▶ HandlerMapping (找到哪个 Controller 处理)
    │        │
    │        ▼
    ├──▶ HandlerAdapter (调用 Controller 方法)
    │        │
    │        ▼
    ├──▶ Controller (业务处理，返回数据或视图名)
    │        │
    │        ▼
    ├──▶ HttpMessageConverter (对象 ↔ JSON/XML 序列化)
    │        │
    │        ▼
    └──▶ Response (返回给客户端)
```

> 注：使用 `@RestController` 时，返回值直接通过 `HttpMessageConverter`（默认 Jackson）序列化为 JSON，不走视图解析。

### 2.2 核心注解速查

| 注解 | 位置 | 作用 |
|------|------|------|
| `@RestController` | 类 | = `@Controller` + `@ResponseBody` |
| `@RequestMapping` | 类/方法 | 映射 URL 路径，支持指定 HTTP 方法 |
| `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` | 方法 | 语义化的请求映射 |
| `@PathVariable` | 参数 | 从 URL 路径提取变量 `/users/{id}` |
| `@RequestParam` | 参数 | 从 Query String 提取参数 `?name=xxx` |
| `@RequestBody` | 参数 | 从请求体反序列化 JSON → 对象 |
| `@RequestHeader` | 参数 | 提取请求头 |
| `@CookieValue` | 参数 | 提取 Cookie 值 |
| `@ResponseStatus` | 方法/异常类 | 指定响应状态码 |
| `@CrossOrigin` | 类/方法 | 跨域配置 |

### 2.3 参数校验体系 / Bean Validation

基于 JSR 380（Bean Validation 2.0），Spring Boot 通过 `spring-boot-starter-validation` 集成 Hibernate Validator。

| 注解 | 作用 | 示例 |
|------|------|------|
| `@NotNull` | 不能为 null | 必填字段 |
| `@NotBlank` | 不能为 null 且 trim 后非空 | 字符串必填 |
| `@NotEmpty` | 不能为 null 且不为空 | 集合/字符串非空 |
| `@Size(min, max)` | 长度/大小范围 | 用户名 2-20 字符 |
| `@Min` / `@Max` | 数值范围 | 年龄 0-150 |
| `@Email` | 邮箱格式 | 邮箱字段 |
| `@Pattern(regexp)` | 正则匹配 | 手机号格式 |
| `@Past` / `@Future` | 日期在过去/未来 | 生日/预约时间 |
| `@Valid` | 触发嵌套对象校验 | 级联校验 |
| `@Validated` | 分组校验 | 创建/更新使用不同规则 |

**校验触发方式：**
- Controller 参数加 `@Valid` 或 `@Validated` → 自动校验
- 校验失败抛出 `MethodArgumentNotValidException`（`@RequestBody`）或 `BindException`（表单）

### 2.4 统一异常处理 / Global Exception Handling

```
┌─────────────────────────────────────────────────┐
│              @RestControllerAdvice               │
│                                                  │
│  @ExceptionHandler(MethodArgumentNotValidException)│
│  → 400 参数校验失败                                │
│                                                  │
│  @ExceptionHandler(BusinessException)             │
│  → 自定义业务异常码                                 │
│                                                  │
│  @ExceptionHandler(Exception)                     │
│  → 500 兜底处理                                   │
└─────────────────────────────────────────────────┘
```

**统一响应体设计建议：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 业务状态码（0=成功，非0=失败） |
| `message` | String | 提示信息 |
| `data` | T | 响应数据 |
| `timestamp` | long | 时间戳 |

### 2.5 拦截器 vs 过滤器 vs AOP

| 特性 | Filter | HandlerInterceptor | AOP |
|------|--------|-------------------|-----|
| 规范 | Servlet | Spring MVC | Spring |
| 作用范围 | 所有请求（含静态资源） | Controller 方法 | 任意 Spring Bean |
| 能否获取 Handler 信息 | 否 | 是 | 是 |
| 典型用途 | 编码、CORS、压缩 | 登录检查、日志、权限 | 事务、缓存、审计 |
| 执行顺序 | 最外层 | 中间层 | 最内层 |

**拦截器执行时机：**

```
Filter → DispatcherServlet → preHandle → Controller → postHandle → afterCompletion → Filter
```

### 2.6 文件上传下载

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.servlet.multipart.enabled` | `true` | 启用文件上传 |
| `spring.servlet.multipart.max-file-size` | `1MB` | 单文件大小限制 |
| `spring.servlet.multipart.max-request-size` | `10MB` | 请求总大小限制 |
| `spring.servlet.multipart.file-size-threshold` | `0B` | 超过此值写入临时文件 |

## 3. 快速集成 / Quick Start

### 3.1 Maven 依赖

| 依赖 | 说明 |
|------|------|
| `spring-boot-starter-web` | Spring MVC + 内嵌 Tomcat + Jackson |
| `spring-boot-starter-validation` | Bean Validation (Hibernate Validator) |

### 3.2 关键配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | `8080` | 服务端口 |
| `server.servlet.context-path` | `/` | 上下文路径 |
| `spring.jackson.date-format` | — | JSON 日期格式 |
| `spring.jackson.time-zone` | — | JSON 时区 |
| `spring.jackson.default-property-inclusion` | — | JSON null 值处理 |
| `spring.mvc.throw-exception-if-no-handler-found` | `true` | 404 抛异常 |

## 4. 进阶要点 / Advanced Topics

- **`ResponseBodyAdvice`** — 统一包装响应体，避免每个 Controller 手动包装
- **`RequestBodyAdvice`** — 请求体预处理，如解密、日志记录
- **自定义 `HandlerMethodArgumentResolver`** — 自定义参数解析，如从 Token 中提取当前用户
- **自定义 `HttpMessageConverter`** — 支持自定义序列化格式（Protobuf、MessagePack 等）
- **内容协商（Content Negotiation）** — 同一接口根据 Accept 头返回 JSON 或 XML
- **异步请求处理** — `Callable<T>` / `DeferredResult<T>` / `StreamingResponseBody` 用于长耗时请求
- **CORS 全局配置** — `WebMvcConfigurer#addCorsMappings` 比 `@CrossOrigin` 更统一
- **接口版本管理** — URL 路径版本 (`/api/v1/`) vs Header 版本 vs 自定义注解
- **`ProblemDetail`（RFC 7807）** — Spring Boot 4.x 标准错误响应格式，统一异常返回结构

## 5. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 404 但 Controller 存在 | 扫描路径不对或 context-path 未加 | 检查 `@ComponentScan` 和请求 URL |
| `@RequestBody` 接收为 null | Content-Type 不是 `application/json` | 检查请求头 |
| 校验不生效 | 缺少 `@Valid` 或未引入 validation starter | 参数前加 `@Valid`，确认依赖 |
| 日期格式化异常 | Jackson 默认输出 timestamp | 配置 `spring.jackson.date-format` 或用 `@JsonFormat` |
| 中文乱码 | 编码不一致 | 确认 `produces = "application/json;charset=UTF-8"` |
| 文件上传超限 | 默认 1MB | 调整 `spring.servlet.multipart.max-file-size` |
| 拦截器不生效 | 未注册到 `WebMvcConfigurer` | 实现 `addInterceptors` 方法注册 |

## 6. 示例项目 / Example

> 示例项目位于 [`examples/spring-mvc-demo/`](../../examples/spring-mvc-demo/)（待创建）
>
> 将演示：RESTful CRUD、参数校验、统一异常处理、统一响应包装、拦截器、文件上传下载

## 7. 参考链接 / References

- [Spring Framework Reference — Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [Spring Boot Reference — Web](https://docs.spring.io/spring-boot/reference/web/servlet.html)
- [Baeldung — Spring MVC Tutorial](https://www.baeldung.com/spring-mvc-tutorial)
- [Baeldung — Spring Validation](https://www.baeldung.com/spring-boot-bean-validation)

## 8. 下一步

掌握了 MVC 开发之后，下一步了解 Spring Boot 的自动配置机制 — 理解 `@SpringBootApplication` 背后的魔法，学会通过 Profile 管理多环境配置，以及如何自定义 Starter。

→ [自动配置与 Starter](03-auto-configuration.md)
