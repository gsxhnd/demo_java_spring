# Spring 生态学习项目 代码描述

## 概述

本目录包含 Spring 生态学习项目各模块的代码描述和设计决策文档。每个文档解释对应模块的职责、设计理由、关键项目和注意事项。

## 模块总览

| 编号 | 模块 | 职责 | 文档 |
|:----:|------|------|------|
| 01 | 核心基础 (core) | IoC 容器、MVC、自动配置、事务管理 | [01-module-core.md](./01-module-core.md) |
| 02 | 数据库集成 (database) | 关系型/NoSQL/搜索引擎/时序数据库接入 | [02-module-database.md](./02-module-database.md) |
| 03 | 框架核心 (framework) | Security、AOP、监控、测试等框架能力 | [03-module-framework.md](./03-module-framework.md) |
| 04 | 微服务 (microservice) | 服务治理、通信、容错、可观测性 | [04-module-microservice.md](./04-module-microservice.md) |
| 05 | 进阶主题 (advanced) | 响应式、批处理、WebSocket、容器化 | [05-module-advanced.md](./05-module-advanced.md) |
| 06 | 示例代码组织 (examples) | 独立示例项目的结构和约定 | [06-module-examples.md](./06-module-examples.md) |

## 命名规则

- 文件名格式：`{编号}-module-{模块名}.md`
- 编号按学习路径排序：核心基础 → 数据库 → 框架 → 微服务 → 进阶 → 示例组织
- 模块名使用小写英文 + 连字符

## 文档维护

- 新增主题时，在本目录下创建对应的文档文件
- 示例项目重构后，更新对应文档的内容
- 主题合并或废弃后，将对应文档标记为已废弃或删除
