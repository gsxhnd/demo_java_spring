# 开发路线图

## 概述

Spring 生态学习项目的开发分为以下阶段，每个阶段有明确的交付物和验收标准。

## Phase 1 — 核心基础篇

**目标**：建立项目骨架，完成 Spring 核心基础文档和示例项目

**交付物**：
- 项目根 README + 文档中心导航
- 核心基础篇文档（IoC & DI, Spring MVC, 自动配置与 Starter, 事务管理）
- 对应示例项目（spring-ioc-demo, spring-mvc-demo, spring-autoconfig-demo, spring-transaction-demo）
- Docker Compose 中间件编排
- 快速开始指南

**验收标准**：
- 所有示例项目独立编译通过
- 文档中代码示例与示例项目代码一致
- 快速开始指南可引导新人完成首次运行

**状态**：✅ 已完成

---

## Phase 2 — 数据库篇与框架核心篇

**目标**：覆盖常用数据库集成和框架核心能力

**交付物**：
- 数据库篇文档（MySQL, PostgreSQL, Redis, MongoDB, Elasticsearch, ClickHouse, InfluxDB）
- 框架核心篇文档（Security+JWT, AOP, Actuator, 日志, 测试, OpenAPI, MyBatis, JPA深入, 缓存, 调度, 异步, 校验, 文件上传）
- 对应示例项目（逐个创建）

**验收标准**：
- 每个数据库示例项目连接对应中间件完成 CRUD 操作
- Security 示例项目实现 JWT 认证授权全流程

**状态**：⏳ 文档已完成，示例项目部分待生成

---

## Phase 3 — 微服务篇

**目标**：建立微服务架构学习体系

**交付物**：
- 微服务篇文档（Gateway, 服务发现, 配置中心, 服务间通信, 熔断降级, 分布式事务, 消息队列, 可观测性）
- 微服务综合示例项目（多模块）

**验收标准**：
- 微服务示例项目包含 gateway + user-service + order-service
- 服务间可正常通信，熔断规则生效
- 配置中心可动态刷新配置

**状态**：⏳ 文档已完成，示例项目待生成

---

## Phase 4 — 进阶主题篇

**目标**：覆盖进阶和高级主题

**交付物**：
- 进阶主题篇文档（WebSocket/SSE, Spring Batch, Spring WebFlux, Spring Modulith, Docker 部署）
- 对应示例项目

**验收标准**：
- WebSocket 示例实现实时聊天
- Batch 示例完成 ETL 流程
- WebFlux 示例演示响应式编程

**状态**：⏳ 文档已完成，示例项目部分待生成

---

## Phase 5 — 版本升级与优化

**目标**：统一升级至目标版本，完善内容

**交付物**：
- 所有示例项目升级至 Spring Boot 4.0.5 + Java 21
- 补充 Spring Cloud Alibaba 集成示例（Sentinel, Seata, RocketMQ）
- 补充 TestContainers 集成测试
- Kubernetes 部署方案文档

**验收标准**：
- 所有示例项目在目标版本下编译通过
- 测试覆盖率 ≥ 60%

**状态**：📅 计划中

---

## 里程碑总览

| 阶段 | 目标 | 关键交付物 | 状态 |
|------|------|------------|------|
| Phase 1 | 核心基础 | 文档 + 4 个示例项目 | ✅ 已完成 |
| Phase 2 | 数据库与框架 | 仅文档 | ⏳ 待开始 |
| Phase 3 | 微服务 | 仅文档 | ⏳ 待开始 |
| Phase 4 | 进阶主题 | 仅文档 | ⏳ 待开始 |
| Phase 5 | 升级优化 | 全项目升级 + 补充 | 📅 计划中 |

## 测试要求

- 每个示例项目需包含基础单元测试
- 数据库/中间件相关示例包含集成测试
- Phase 5 完成后全项目测试覆盖率 ≥ 60%
