# Spring 生态学习项目 开发文档

## 概述

一站式学习 Java Spring Boot + Spring Cloud 全生态。

本目录是 Spring 生态学习项目的开发文档，包含产品定义、架构设计、领域模型、技术栈说明、开发路线图和待决问题。

## 阅读顺序

| 文件 | 内容 |
|------|------|
| `01-product-scope.md` | 产品定位、需求、MVP 范围与验收标准 |
| `02-architecture.md` | 系统分层、模块职责、依赖关系 |
| `03-domain-model.md` | 核心类型、接口定义、数据流 |
| `04-tech-stack.md` | 语言版本、依赖管理、工具链、常用命令 |
| `05-roadmap.md` | 开发路线图、阶段划分、里程碑 |
| `06-open-questions.md` | 未决设计问题与决策记录 |

**建议阅读顺序**：`01` → `02` → `03` → `04` → `05` → `06`

## 文档规则

- `docs/dev/` 是当前唯一有效的开发文档源
- 产品范围以 `01-product-scope.md` 为准
- 架构边界以 `02-architecture.md` 与 `03-domain-model.md` 为准
- 开发顺序以 `05-roadmap.md` 为准
- 未决问题统一记录在 `06-open-questions.md`
- 文档默认为 draft，代码落地后再更新为已验证描述
- 技术参考文档统一存放在 [reference/](../reference/00-readme.md) 目录下
- 模块设计决策文档存放在 [wiki/](../wiki/00-readme.md) 目录下
- 用户使用指南存放在 [usage/](../usage/00-readme.md) 目录下

## 设计原则

- 每个示例项目必须独立可运行，无交叉依赖
- 文档中英对照，代码注释使用英文
- 中间件通过 Docker Compose 统一管理，避免本地安装依赖
- 版本矩阵在主 README 中统一维护
