# Spring 生态学习项目 开发文档

一站式学习 Java Spring Boot + Spring Cloud 全生态。

## 目标读者

| 如果你是... | 推荐阅读 |
|------------|----------|
| 贡献者、维护者 | → [项目架构](./01-architecture.md)、[技术栈](./02-tech-stack.md) |
| 初次使用 | → [快速开始](./03-quick-start.md) |
| 了解配置 | → [配置说明](./04-configuration.md) |
| 日常学习 | → [学习指南](./05-learning-guide.md) |
| 遇到问题 | → [故障排查](./06-troubleshooting.md) |
| 有疑问 | → [常见问题](./07-faq.md) |
| 查阅技术细节 | → [技术参考文档](../reference/README.md) |

## 文档索引

| 文件 | 说明 |
|------|------|
| `01-architecture.md` | 项目定位、架构设计、模块职责、领域模型 |
| `02-tech-stack.md` | 版本矩阵、核心依赖、开发环境搭建 |
| `03-quick-start.md` | 环境准备、安装、30 秒快速上手 |
| `04-configuration.md` | Docker Compose 中间件配置、端口规划 |
| `05-learning-guide.md` | 推荐学习路径、进阶技巧 |
| `06-troubleshooting.md` | 常见错误与解决方案 |
| `07-faq.md` | 常见问题解答 |

**建议阅读顺序**：`01` → `02` → `03` → `04` → `05`，遇到问题查阅 `06` 和 `07`。

## 设计原则

- 每个示例项目必须独立可运行，无交叉依赖
- 中文为主，技术术语保持英文
- 中间件通过 Docker Compose 统一管理
- 版本矩阵在 `02-tech-stack.md` 中统一维护
