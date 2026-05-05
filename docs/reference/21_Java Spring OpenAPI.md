---
title: Java Spring OpenAPI
created: 2026-05-05 12:16:00
category: Java-Spring
tags:
  - Java
  - Spring
  - OpenAPI
  - Swagger
  - API-Documentation
---

<!-- markdownlint-disable MD025 -->

# Java Spring OpenAPI

## 为什么要学 OpenAPI

前面几节完成了 Web 开发的核心链路：Controller 定义路由、请求处理绑定参数、DTO 规范响应格式、异常处理统一错误输出、参数校验保证数据合法。API 能正常工作了。

但接下来有一个实际问题：**前端怎么知道你的接口长什么样？**

手动写 API 文档（Word、Markdown）容易过时 -- 代码改了文档忘了更新。OpenAPI 规范 + springdoc 库可以从代码中自动生成 API 文档，并提供交互式的 Swagger UI 供调试。文档和代码始终同步，零维护成本。

---

## 核心概念

### OpenAPI 是什么

**OpenAPI 是描述 RESTful API 的行业标准规范（当前版本 3.1）。** 它用 JSON 或 YAML 格式定义 API 的路径、参数、请求体、响应、认证方式等所有信息。Swagger UI 是基于 OpenAPI 规范自动生成的交互式文档界面。

类比：OpenAPI 规范就像建筑的蓝图。蓝图（规范文件）描述了建筑的结构，施工队（前端开发者）按蓝图施工，验收人员（测试）按蓝图验收。Swagger UI 则是蓝图的 3D 可视化展示，让所有人都能直观理解。

### 为什么需要 OpenAPI

API 是前后端协作的契约。没有标准化的描述方式，沟通只能靠口头、聊天记录或手写文档 -- 这些都容易过时和产生歧义。OpenAPI 规范让 API 描述机器可读，可以自动生成文档、客户端 SDK、Mock 服务器、测试用例。

### 没有 OpenAPI 会怎样

前端开发者需要反复问后端"这个接口参数是什么"、"返回格式是什么"。手写的文档和实际代码不一致时，联调阶段会浪费大量时间排查"文档说的和实际不一样"的问题。有了 OpenAPI + springdoc，文档从代码自动生成，永远和代码同步。

---

## 概念深入解释

### springdoc-openapi 工作原理

```
Controller 注解 + DTO 类定义
        │
        ▼
springdoc 在启动时扫描所有 @RestController
        │
        ▼
解析 @GetMapping、@PostMapping、@PathVariable、@RequestBody 等
        │
        ▼
读取 Bean Validation 注解（@NotNull、@Size 等）作为参数约束
        │
        ▼
生成 OpenAPI 3.0 规范文件（JSON/YAML）
        │
        ▼
Swagger UI 渲染为交互式文档页面
```

springdoc 是 Spring Boot 3.x 推荐的 OpenAPI 文档库（替代了旧的 springfox）。

### 引入依赖

```xml
<!-- Maven -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.0</version>
</dependency>
```

引入后无需额外配置，启动应用即可访问：

- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

### 常用注解

springdoc 会自动从 Spring MVC 注解推断大部分信息，但你可以用额外注解补充描述：

| 注解 | 位置 | 作用 |
|------|------|------|
| `@Tag(name = "用户管理")` | Controller 类 | 接口分组 |
| `@Operation(summary = "创建用户")` | Controller 方法 | 接口描述 |
| `@Parameter(description = "用户ID")` | 方法参数 | 参数描述 |
| `@Schema(description = "用户名")` | DTO 字段 | 字段描述 |
| `@ApiResponse(responseCode = "201")` | Controller 方法 | 响应描述 |

### 使用示例

```java
@Tag(name = "用户管理", description = "用户 CRUD 接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Operation(summary = "根据 ID 查询用户")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @ApiResponse(responseCode = "404", description = "用户不存在")
    @GetMapping("/{id}")
    public UserVO getById(
        @Parameter(description = "用户 ID", example = "1")
        @PathVariable Long id) {
        // ...
    }
}
```

DTO 上的 `@Schema` 注解：

```java
public class CreateUserDTO {
    @Schema(description = "用户名", example = "tom", minLength = 3, maxLength = 20)
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @Schema(description = "邮箱地址", example = "tom@example.com")
    @Email
    private String email;
}
```

### 配置选项

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs          # OpenAPI 规范文件路径
  swagger-ui:
    path: /swagger-ui.html      # Swagger UI 路径
    tags-sorter: alpha           # 标签按字母排序
    operations-sorter: method    # 接口按 HTTP 方法排序
  default-produces-media-type: application/json
  show-actuator: false           # 不展示 Actuator 端点
```

### 生产环境安全

生产环境通常不应该公开暴露 API 文档：

```yaml
# application-prod.yml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

或者通过 Spring Security 限制访问：只允许内部网络或特定角色访问 `/swagger-ui.html` 和 `/v3/api-docs`。

### 与 Bean Validation 的联动

springdoc 会自动读取 DTO 上的校验注解，生成对应的约束描述：

| 校验注解 | OpenAPI 描述 |
|----------|-------------|
| `@NotNull` | `required: true` |
| `@Size(min=3, max=20)` | `minLength: 3, maxLength: 20` |
| `@Min(1)` | `minimum: 1` |
| `@Email` | `format: email` |
| `@Pattern(regexp)` | `pattern: regexp` |

这意味着你不需要在 `@Schema` 中重复声明约束 -- 校验注解已经提供了这些信息。

---

## 核心要点

1. **引入 springdoc 依赖即可零配置使用。** 不需要额外注解，springdoc 会从 Spring MVC 注解自动推断 API 结构。
2. **用 `@Tag` 分组、`@Operation` 描述、`@Schema` 补充字段说明。** 这些注解是锦上添花，不是必须的，但能让文档更易读。
3. **Bean Validation 注解自动转为 API 约束。** 不需要在文档注解中重复声明参数限制。
4. **生产环境禁用或限制访问。** API 文档暴露了所有接口细节，是攻击者的信息来源。
5. **springdoc 替代了 springfox。** Spring Boot 3.x 不再兼容 springfox（基于 Swagger 2），新项目统一使用 springdoc（基于 OpenAPI 3）。

---

## 常见误区

- **Spring Boot 3 项目引入 springfox 导致启动失败。** springfox 依赖 `javax.*` 包，Spring Boot 3 迁移到了 `jakarta.*`，两者不兼容。Spring Boot 3 必须使用 springdoc-openapi v2.x。
- **以为必须加大量注解才能生成文档。** springdoc 会自动从 `@GetMapping`、`@RequestBody`、`@PathVariable` 等推断信息。只有需要补充描述文本时才需要额外注解。过度注解反而让代码变得冗长。
- **Swagger UI 能访问但显示空白。** 通常是 Spring Security 拦截了 `/v3/api-docs` 请求。需要在 Security 配置中放行 `/swagger-ui/**` 和 `/v3/api-docs/**`。
- **统一响应包装导致文档中 Response 类型不准确。** 如果用了 `ResponseBodyAdvice` 统一包装响应，springdoc 看到的返回类型是原始类型而非包装后的 `ApiResponse<T>`。需要通过 `@Operation` 的 `@ApiResponse` 显式声明实际返回结构。
- **把 OpenAPI 文档当作唯一的 API 契约。** OpenAPI 描述的是接口的结构，但无法表达业务规则（如"创建订单时库存必须充足"）。复杂的业务约束仍然需要文字说明补充。

---

## 与其他概念的关联

- **前置：** [Java Spring Controller](./16_Java%20Spring%20Controller.md) -- OpenAPI 文档从 Controller 的路由注解生成。[Java Spring 响应与 DTO](./18_Java%20Spring%20响应与%20DTO.md) -- DTO 的字段定义决定了文档中的 Schema。[Java Spring 参数校验](./20_Java%20Spring%20参数校验.md) -- 校验注解自动转为文档中的参数约束。
- **并行：** [Java Spring 异常处理](./19_Java%20Spring%20异常处理.md) -- 错误响应的格式也应该在 OpenAPI 文档中描述。
- **后续：** Part 8 的 [Java Spring Security] 会涉及 Swagger UI 的访问控制配置。API 文档也是前后端联调和自动化测试的基础。
