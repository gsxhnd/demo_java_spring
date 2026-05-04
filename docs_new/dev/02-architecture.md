# 系统架构

## 架构概述

Spring 生态学习项目采用 **Monorepo + 独立示例项目** 架构。根项目为文档中心，`examples/` 下每个子目录为独立的 Spring Boot 项目。

## 分层设计

```text
┌─────────────────────────────────────────────┐
│              docs/ — 文档层                  │
│   core/ | database/ | framework/ |          │
│   microservice/ | advanced/                 │
└──────────────────────┬──────────────────────┘
                       │ 引用对应
                       ▼
┌─────────────────────────────────────────────┐
│         examples/ — 示例代码层               │
│   每个子目录 = 独立可运行的 Spring Boot 项目  │
│   spring-mvc-demo/ | spring-mysql-demo/ |   │
│   spring-security-demo/ | ... (21 个项目)    │
└──────────────────────┬──────────────────────┘
                       │ 依赖
                       ▼
┌─────────────────────────────────────────────┐
│    docker-compose/ — 基础设施层              │
│   MySQL | Redis | MongoDB | ES | ...        │
└─────────────────────────────────────────────┘
```

## 模块职责

| 模块 | 职责 | 关键文件 |
|------|------|----------|
| `docs/` | 学习文档，按主题组织 | `docs/README.md` |
| `docs/core/` | Spring 核心基础文档 | `docs/core/ioc-di.md` |
| `docs/database/` | 数据库集成文档 | `docs/database/mysql.md` |
| `docs/framework/` | 框架核心能力文档 | `docs/framework/security-jwt.md` |
| `docs/microservice/` | 微服务架构文档 | `docs/microservice/gateway.md` |
| `docs/advanced/` | 进阶主题文档 | `docs/advanced/webflux.md` |
| `examples/` | 独立可运行的示例项目 | `examples/README.md` |
| `examples/docker-compose/` | 中间件容器编排 | `examples/docker-compose/full-stack-compose.yml` |

## 依赖关系

```text
docs/ (文档层 — 无代码依赖)
  ↑ 引用
examples/ (示例代码层 — 每个项目独立，无交叉依赖)
  ↑ 运行时依赖
docker-compose/ (基础设施层 — 纯配置文件，无代码)
```

**依赖规则**：

- `examples/` 下每个子目录必须是独立可编译运行的 Spring Boot 项目，禁止子项目间交叉依赖
- 示例项目只通过 `spring-boot-starter-parent` 和 Maven 中央仓库获取依赖，不依赖项目内部其他模块
- 中间件通过 Docker Compose 统一提供，示例项目通过 `application.yml` 连接
- 文档中引用的代码片段应与对应示例项目保持一致
- 版本号统一在根 `README.md` 的版本矩阵中维护

## 运行时模型

- 各示例项目为独立的 Spring Boot 应用，通过内嵌 Tomcat/Netty 提供 HTTP 服务
- 默认端口 8080（可通过 `application.yml` 修改）
- 中间件通过 Docker 容器运行，项目启动时自动连接
- 无共享状态，各示例项目可并行运行（需注意端口冲突）

## 错误处理策略

- 示例项目遵循 Spring Boot 标准错误处理机制，通过 `@ControllerAdvice` 统一处理异常
- 中间件不可用时，应用启动失败并给出明确错误信息
- 文档中的代码示例包含常见错误场景和处理方式
