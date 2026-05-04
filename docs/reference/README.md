# 技术参考文档

本目录包含 Spring 生态学习项目的所有深度技术参考文档。每个文档覆盖一个技术主题，包含核心概念、配置示例、代码片段、进阶要点和常见问题。

## 篇章导航

| 篇章 | 说明 | 入口 |
|------|------|------|
| 核心基础篇 | IoC、MVC、自动配置、事务管理 | [01-core/README.md](./01-core/README.md) |
| 数据库篇 | MySQL、Redis、MongoDB、ES 等 7 种数据库 | [02-database/README.md](./02-database/README.md) |
| 框架核心篇 | Security、AOP、Actuator 等 13 个框架能力 | [03-framework/README.md](./03-framework/README.md) |
| 微服务篇 | Gateway、Nacos、Feign 等 8 个微服务组件 | [04-microservice/README.md](./04-microservice/README.md) |
| 进阶主题篇 | WebSocket、Batch、WebFlux、Modulith、Docker | [05-advanced/README.md](./05-advanced/README.md) |

## 学习路线

```
核心基础篇 → 数据库篇 → 框架核心篇 → 微服务篇 → 进阶主题篇
```

核心基础篇是所有其他篇章的依赖基座，建议优先学习。数据库篇和框架核心篇可并行，微服务篇依赖前三者，进阶主题篇可按需选学。

## 文档规范

- 每个文档结构：概述 → 核心概念 → 快速集成 → 进阶要点 → 常见问题 → 示例项目 → 参考链接
- 中文为主，技术术语保持英文
- 配置示例与 Docker Compose 保持一致
