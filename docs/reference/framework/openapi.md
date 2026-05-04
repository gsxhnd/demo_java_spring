# OpenAPI — API 文档 / API Documentation

> SpringDoc OpenAPI 3 + Swagger UI：自动生成、在线调试

## 1. 概述 / Overview

OpenAPI 3.0 是 REST API 的标准描述规范（前身是 Swagger）。SpringDoc 是 Spring Boot 4.x 推荐的 OpenAPI 文档生成库，自动扫描 Controller 生成 API 文档，并提供 Swagger UI 在线调试界面。

### SpringDoc vs SpringFox

| 特性 | SpringDoc | SpringFox |
|---|---|---|
| OpenAPI 版本 | **3.0 / 3.1** | 2.0 (Swagger) |
| Spring Boot 4.x | 支持 | 不支持（已停更） |
| 维护状态 | 活跃 | 停更 |
| WebFlux 支持 | 支持 | 有限 |
| 推荐 | 新项目必选 | 不推荐 |

---

## 2. 核心概念 / Core Concepts

### OpenAPI 文档结构

```
OpenAPI Spec (JSON/YAML)
  ├── info          ← API 基本信息（标题、版本、描述）
  ├── servers       ← 服务器地址
  ├── paths         ← API 路径和操作
  │   └── /api/users
  │       ├── GET   ← 查询用户列表
  │       ├── POST  ← 创建用户
  │       └── ...
  ├── components    ← 可复用组件
  │   ├── schemas   ← 数据模型（DTO）
  │   └── securitySchemes ← 认证方案（JWT Bearer）
  └── security      ← 全局安全要求
```

### 核心注解

| 注解 | 位置 | 说明 |
|---|---|---|
| `@Tag` | Controller 类 | API 分组标签 |
| `@Operation` | 方法 | 接口描述（summary、description） |
| `@Parameter` | 参数 | 参数描述 |
| `@Schema` | DTO 字段 | 字段描述、示例值、校验规则 |
| `@ApiResponse` | 方法 | 响应描述（状态码、描述） |
| `@SecurityRequirement` | 类/方法 | 接口需要的认证方式 |
| `@Hidden` | 类/方法 | 隐藏不展示 |

---

## 3. 快速集成 / Quick Start

### Maven 依赖

- `org.springdoc:springdoc-openapi-starter-webmvc-ui`（Spring MVC）
- 或 `org.springdoc:springdoc-openapi-starter-webflux-ui`（WebFlux）

### 关键配置

| 配置 | 说明 |
|---|---|
| `springdoc.api-docs.path` | OpenAPI JSON 路径（默认 `/v3/api-docs`） |
| `springdoc.swagger-ui.path` | Swagger UI 路径（默认 `/swagger-ui.html`） |
| `springdoc.swagger-ui.tags-sorter` | 标签排序方式 |
| `springdoc.swagger-ui.operations-sorter` | 操作排序方式 |
| `springdoc.packages-to-scan` | 扫描的包路径 |
| `springdoc.default-produces-media-type` | 默认响应类型 |

### 访问地址

| 地址 | 说明 |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Swagger UI 界面 |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON |
| `http://localhost:8080/v3/api-docs.yaml` | OpenAPI YAML |

---

## 4. 进阶要点 / Advanced Topics

- **分组 (Group)**：`springdoc.group-configs` 按模块分组，不同 URL 前缀展示不同 API
- **JWT 认证配置**：全局 `SecurityScheme`（Bearer Token），Swagger UI 中可输入 Token 调试
- **自定义 OpenApiCustomizer**：编程方式修改 OpenAPI 文档（添加全局 Header、修改描述）
- **DTO 校验集成**：`@Schema` + `@NotNull` / `@Size` 等 Bean Validation 注解自动展示校验规则
- **枚举展示**：`@Schema(allowableValues = {"ACTIVE", "INACTIVE"})` 展示枚举值
- **文件上传**：`@Parameter(content = @Content(mediaType = "multipart/form-data"))`
- **生产环境禁用**：`springdoc.api-docs.enabled=false` 生产环境关闭文档
- **导出为静态文档**：OpenAPI JSON → Redoc / Slate 生成静态 HTML 文档

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| Swagger UI 访问 404 | 确认依赖是 `springdoc-openapi-starter-webmvc-ui`（不是旧版 springfox） |
| 接口不显示 | 检查 `packages-to-scan` 配置，或 Controller 是否有 `@RestController` |
| JWT 认证不生效 | 配置全局 `SecurityScheme`，并在 `@SecurityRequirement` 中引用 |
| 生产环境暴露文档 | 设置 `springdoc.api-docs.enabled=false` |
| 响应模型不正确 | 在方法返回类型或 `@ApiResponse` 中明确指定 Schema |

---

## 6. 示例项目 / Example

OpenAPI 配置集成在各示例项目中。

## 7. 参考链接 / References

- [SpringDoc 官方文档](https://springdoc.org/)
- [OpenAPI 3.0 规范](https://spec.openapis.org/oas/v3.0.3)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)
