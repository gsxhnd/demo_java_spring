# Spring MVC Web 开发基础 / Spring MVC Fundamentals

> Spring MVC 是构建 RESTful API 和 Web 应用的核心框架。掌握请求处理、参数校验、异常处理和拦截器，是写好 Web 服务的基本功。

## 1. 概述 / Overview

Spring MVC 基于 Servlet 规范，采用前端控制器模式（Front Controller Pattern）。所有请求经过 `DispatcherServlet` 统一分发，由 Handler（Controller）处理后返回响应。Spring Boot 内嵌 Tomcat，开箱即用。

## 2. 术语表 / Glossary

> 以下术语是 Spring MVC 的核心概念。如果不熟悉 IoC / Bean 等基础概念，建议先阅读 [IoC 与依赖注入](01-ioc-di.md)。

| 术语 | 定义 | 作用 | 为什么存在 |
|------|------|------|-----------|
| **DispatcherServlet** | Spring MVC 的前端控制器（Front Controller），所有 HTTP 请求的入口。它自己不处理业务，而是把请求分发给对应的 Controller。 | 统一接收请求、协调 HandlerMapping / HandlerAdapter / ViewResolver 三大组件完成请求处理。 | 遵循前端控制器设计模式——将通用的横切逻辑（编码、权限、日志）集中在入口处，避免每个 Servlet 重复实现。 |
| **Controller（处理器）** | 实际处理 HTTP 请求的组件，标注 `@RestController` 或 `@Controller`。方法上通过 `@GetMapping`、`@PostMapping` 等注解映射 URL。 | 接收请求参数 → 调用 Service 层 → 返回响应。是 HTTP 协议和业务逻辑之间的适配层。 | 将"HTTP 请求分发"和"业务处理"解耦。DispatcherServlet 只管"谁处理"，Controller 只管"怎么处理"。 |
| **`@RestController`** | `@Controller` + `@ResponseBody` 的组合注解。方法的返回值直接序列化为 JSON/XML 写入响应体，不走视图渲染。 | 构建 RESTful API 的标准注解。 | REST API 不需要返回 HTML 页面，只需返回数据。`@RestController` 省去了每个方法都加 `@ResponseBody` 的繁琐。 |
| **HttpMessageConverter** | 负责 HTTP 请求体 ↔ Java 对象的双向转换。默认使用 Jackson 做 JSON 序列化/反序列化。 | 将 `@RequestBody` 标记的参数从 JSON 转为 Java 对象，将 Controller 返回的对象转为 JSON 响应体。 | HTTP 传输的是字节流，Java 操作的是对象。`HttpMessageConverter` 做透明的双向转换，开发者无需手动处理序列化。 |
| **HandlerInterceptor（拦截器）** | 在 Controller 方法执行前后插入逻辑的组件。通过 `preHandle` / `postHandle` / `afterCompletion` 三个回调实现。 | 实现登录检查、权限验证、请求日志、性能计时等横切逻辑。 | 和 Filter 不同，拦截器能拿到 Handler 对象（知道是哪个 Controller 方法），能对特定 URL 模式精确控制。 |
| **Filter（过滤器）** | Servlet 规范层面的组件，在请求到达 DispatcherServlet **之前**执行。 | 处理字符编码、CORS、请求压缩等 Servlet 层面的通用逻辑。 | 拦截器和 Filter 各有领地：Filter 负责底层协议工作，Interceptor 负责业务相关的请求处理。 |
| **`@RestControllerAdvice`** | 全局的 AOP 增强切面，作用于所有 `@RestController`。 | 配合 `@ExceptionHandler` 统一处理所有 Controller 抛出的异常；配合 `ResponseBodyAdvice` 统一包装响应体。 | 将异常处理和响应格式从 Controller 中剥离。Controller 只需抛出异常或返回业务数据，格式化和错误映射由 Advice 层集中处理。 |
| **`@ResponseBodyAdvice`** | 在响应体序列化之前插入逻辑的接口。 | 在 Controller 返回值和 JSON 序列化之间做一次"拦截"，比如统一包装成 `{"code":0,"data":...}` 格式。 | 避免每个 Controller 手动调用包装方法。一次实现，全局生效——跨切关注点的典型应用。 |
| **ResponseEntity** | Spring 对 HTTP 响应的完整封装，包含状态码、响应头、响应体。 | 当需要精确控制 HTTP 响应时使用（如文件下载需要设置 `Content-Disposition` 头）。 | `@RestController` 适合 JSON 响应，`ResponseEntity` 适合需要自定义 HTTP 协议细节的场景（文件下载、流式响应）。 |
| **`@Valid` / Bean Validation** | 对方法参数进行声明式校验的注解，基于 JSR 380（Jakarta Bean Validation）规范。 | 在 Controller 参数上标注 `@Valid`，Spring 自动校验 `@NotBlank`、`@Email` 等约束，失败时抛出 `MethodArgumentNotValidException`。 | 将校验逻辑从业务代码中解耦——不是在 Service 层写 if-else 判断，而是在 DTO 上用注解声明规则，既清晰又可复用。 |
| **DTO（Data Transfer Object）** | 专门用于数据传输的纯数据对象，不包含业务逻辑。通常与 API 的 Request/Response 一一对应。 | 隔离外部协议和内部模型——请求体用 `UserRequest`（含校验注解），内部业务代码用 `User`（含业务字段）。 | 前端传什么格式和数据库存什么结构是两件事。DTO 作为中间层，防止 API 协议变化直接冲击内部模型。 |

## 3. 核心概念 / Core Concepts

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

## 4. 快速集成 / Quick Start

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

## 5. 设计决策与实现原理 / Design Decisions

> 以下结合 [`examples/spring-mvc-demo/`](../../examples/spring-mvc-demo/) 的实际代码，解释每个设计选择背后的"为什么"。

### 4.1 为什么用 `ResponseBodyAdvice` 自动包装而非每个 Controller 手动调用 `ApiResponse.success()`？

```java
// GlobalResponseBodyAdvice.java — 一次配置，全局生效
@RestControllerAdvice
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public Object beforeBodyWrite(...) {
        return ApiResponse.success(body);
    }
}
```

- **DRY 原则**：避免每个 Controller 方法末尾都写 `return ApiResponse.success(data)`
- **防止遗漏**：人工包装容易忘记，`ResponseBodyAdvice` 保证所有响应都被统一格式
- **关注点分离**：Controller 只返回业务数据，响应格式是横切关注点，应由 Advice 层处理
- **`supports()` 防重复包装**：检查返回值类型，若已是 `ApiResponse` 则跳过（如 `GlobalExceptionHandler` 的返回值），避免出现 `ApiResponse(code=0, data=ApiResponse(code=500, ...))` 这种嵌套结构

### 4.2 为什么 `GlobalExceptionHandler` 中异常处理方法有特定顺序？

```java
@ExceptionHandler(MethodArgumentNotValidException.class)  // 400 校验失败
@ExceptionHandler(UserNotFoundException.class)            // 404 资源不存在
@ExceptionHandler(IllegalArgumentException.class)          // 400 参数错误
@ExceptionHandler(Exception.class)                         // 500 兜底
```

Spring 选择 `@ExceptionHandler` 时按**异常类型匹配精度**决定（不是按方法声明顺序）。从最具体的子类到最通用的父类排列，是**可读性约定**——让维护者一眼看清异常处理的层级结构。

### 4.3 为什么使用 `MultipartFile` (单文件) 和 `MultipartFile[]` (多文件) 两个独立接口？

教学目的——分别展示：
- **单文件上传**：`@RequestParam("file") MultipartFile file` — 直接绑定到单个 `MultipartFile`
- **多文件上传**：`@RequestParam("files") MultipartFile[] files` — Spring 自动将多个同名参数收集为数组

实际项目中通常会合并为一个接口，通过参数类型灵活处理。

### 4.4 为什么文件下载使用 `ResponseEntity<byte[]>` 而非 `ApiResponse<byte[]>`？

```java
@GetMapping("/download/{filename}")
public ResponseEntity<byte[]> downloadFile(@PathVariable String filename) {
    // ...
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(bytes);
}
```

- **二进制数据不应包装为 JSON**：`ApiResponse` 是 JSON 外壳，文件内容是二进制流
- **需要自定义响应头**：文件下载必须设置 `Content-Disposition`、`Content-Type` 等头
- **`ResponseEntity` 提供完整的 HTTP 响应控制**：绕过 `ResponseBodyAdvice` 的统一包装（因为返回类型不是业务数据）

### 4.5 为什么拦截器排除 `/api/health` 和 `/api/public/**`？

```java
registry.addInterceptor(loggingInterceptor)
        .addPathPatterns("/api/**")
        .excludePathPatterns("/api/health", "/api/public/**");
```

- **健康检查被高频调用**（K8s liveness/readiness probe 每 10-30 秒一次），日志打印会造成噪音
- **公开接口无需认证/审计开销**：`/api/public/*` 的请求不需要安全拦截，排除后减少不必要的计算
- **`excludePathPatterns` 在拦截器层面即短路**，被排除的路径不会进入 `preHandle` / `postHandle` / `afterCompletion`

### 4.6 为什么使用 HandlerInterceptor 而非 Filter？

| 对比维度 | Filter (Servlet) | HandlerInterceptor (Spring MVC) |
|---------|------------------|-------------------------------|
| 能获取 Handler 信息 | 否 | 是 (`Object handler` 参数) |
| 能访问 Spring Bean | 需额外配置 | 天然可注入 (`@Component`) |
| URL 模式匹配 | 通配符 (`/*`) | Ant 风格 (`/api/**`)，支持排除路径 |
| 请求体读取 | 会消费 InputStream | 不消费（通过 `ContentCachingRequestWrapper` 读取） |

对于"记录请求耗时"这个场景，Interceptor 更合适——它能拿到 Handler 方法信息用于日志。

### 4.7 为什么开启虚拟线程 `server.threading.virtual-thread-enabled: true`？

Java 21 的虚拟线程（Project Loom）将线程资源从操作系统线程池中解耦。Spring Boot 4.0 自动检测 Java 21+，配合此配置让 Tomcat 使用虚拟线程执行器：
- **高并发 I/O 密集型**：成百上千个虚拟线程共享少量 OS 线程，内存开销极低
- **无需调整线程池大小**：虚拟线程按需创建，不再需要 `server.tomcat.threads.max` 调优

### 4.8 为什么 `spring.jackson.default-property-inclusion: non_null`？

- **减小 JSON 体积**：跳过 `null` 字段，网络传输更高效
- **前端友好**：避免 `"field": null` 造成的类型误判（`typeof null === "object"`）
- **API 语义清晰**："未提供"和"提供了但为 null"在 JSON 层面区分开

### 4.9 为什么日志拦截器用 `request.setAttribute("startTime")` 传递计时起点？

```java
// preHandle
request.setAttribute("startTime", System.currentTimeMillis());

// postHandle
Long startTime = (Long) request.getAttribute("startTime");
long duration = System.currentTimeMillis() - startTime;
```

`preHandle` 和 `postHandle` 是同一个 `HttpServletRequest` 的不同生命周期回调，但方法的返回值无法传参。`request.setAttribute()` 利用 Servlet 规范中 request 属性的请求级作用域，是最简单的跨回调数据传递方式。

### 4.10 为什么 `WebMvcConfig` 中用 `allowedOriginPatterns("*")` 而非 `allowedOrigins("*")`？

```java
registry.addMapping("/api/**")
        .allowedOriginPatterns("*")    // ← 不是 allowedOrigins("*")
        .allowCredentials(true);
```

`allowedOrigins("*")` 与 `allowCredentials(true)` 互斥（CORS 规范禁止通配来源 + 凭据）。`allowedOriginPatterns` 支持通配符模式匹配，是 `allowCredentials(true)` 场景下实现跨域的正确方式。Demo 中设置 `allowCredentials(true)` 是为了演示完整配置，生产环境应限制具体的 origin 列表。

## 6. 进阶要点 / Advanced Topics

- **`ResponseBodyAdvice`** — 统一包装响应体，避免每个 Controller 手动包装
- **`RequestBodyAdvice`** — 请求体预处理，如解密、日志记录
- **自定义 `HandlerMethodArgumentResolver`** — 自定义参数解析，如从 Token 中提取当前用户
- **自定义 `HttpMessageConverter`** — 支持自定义序列化格式（Protobuf、MessagePack 等）
- **内容协商（Content Negotiation）** — 同一接口根据 Accept 头返回 JSON 或 XML
- **异步请求处理** — `Callable<T>` / `DeferredResult<T>` / `StreamingResponseBody` 用于长耗时请求
- **CORS 全局配置** — `WebMvcConfigurer#addCorsMappings` 比 `@CrossOrigin` 更统一
- **接口版本管理** — URL 路径版本 (`/api/v1/`) vs Header 版本 vs 自定义注解
- **`ProblemDetail`（RFC 7807）** — Spring Boot 4.x 标准错误响应格式，统一异常返回结构

## 7. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| 404 但 Controller 存在 | 扫描路径不对或 context-path 未加 | 检查 `@ComponentScan` 和请求 URL |
| `@RequestBody` 接收为 null | Content-Type 不是 `application/json` | 检查请求头 |
| 校验不生效 | 缺少 `@Valid` 或未引入 validation starter | 参数前加 `@Valid`，确认依赖 |
| 日期格式化异常 | Jackson 默认输出 timestamp | 配置 `spring.jackson.date-format` 或用 `@JsonFormat` |
| 中文乱码 | 编码不一致 | 确认 `produces = "application/json;charset=UTF-8"` |
| 文件上传超限 | 默认 1MB | 调整 `spring.servlet.multipart.max-file-size` |
| 拦截器不生效 | 未注册到 `WebMvcConfigurer` | 实现 `addInterceptors` 方法注册 |

## 8. 示例项目 / Example

> 示例项目位于 [`examples/spring-mvc-demo/`](../../examples/spring-mvc-demo/)
>
> 已演示：RESTful CRUD、参数校验（`@Valid` + Jakarta Validation）、统一异常处理（`@RestControllerAdvice`）、统一响应包装（`ResponseBodyAdvice`）、拦截器（`HandlerInterceptor` 日志与计时）、CORS 全局配置、文件上传下载（`MultipartFile` / `ResponseEntity<byte[]>`）、虚拟线程

## 9. 参考链接 / References

- [Spring Framework Reference — Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [Spring Boot Reference — Web](https://docs.spring.io/spring-boot/reference/web/servlet.html)
- [Baeldung — Spring MVC Tutorial](https://www.baeldung.com/spring-mvc-tutorial)
- [Baeldung — Spring Validation](https://www.baeldung.com/spring-boot-bean-validation)

## 10. 下一步

掌握了 MVC 开发之后，下一步了解 Spring Boot 的自动配置机制 — 理解 `@SpringBootApplication` 背后的魔法，学会通过 Profile 管理多环境配置，以及如何自定义 Starter。

→ [自动配置与 Starter](03-auto-configuration.md)
