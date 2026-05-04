# Spring 生态学习项目 开发文档

一站式学习 Java Spring Boot + Spring Cloud 全生态。

本目录是项目的开发文档，面向贡献者和维护者。

## 文档索引

| 文件 | 内容 |
|------|------|
| `01-architecture.md` | 项目定位、架构设计、模块职责、领域模型 |
| `02-tech-stack.md` | 版本矩阵、核心依赖、开发环境搭建 |
| `03-roadmap.md` | 开发路线图、阶段划分、里程碑 |
| `04-open-questions.md` | 未决设计问题与决策记录 |

**建议阅读顺序**：`01` → `02` → `03` → `04`

## 文档规则

- `docs/dev/` 是当前唯一有效的开发文档源
- 架构边界以 `01-architecture.md` 为准
- 开发顺序以 `03-roadmap.md` 为准
- 未决问题统一记录在 `04-open-questions.md`
- 技术参考文档统一存放在 [reference/](../reference/README.md) 目录下
- 用户使用指南存放在 [usage/](../usage/README.md) 目录下

## 设计原则

- 每个示例项目必须独立可运行，无交叉依赖
- 中文为主，技术术语保持英文
- 中间件通过 Docker Compose 统一管理
- 版本矩阵在 `02-tech-stack.md` 中统一维护
