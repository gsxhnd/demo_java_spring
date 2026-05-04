# Spring 生态学习项目 / Spring Ecosystem Learning Project

> 一站式学习 Java Spring Boot + Spring Cloud 全生态

本项目是一个 Spring 生态学习资料库，包含完整的中英对照文档和独立可运行的示例项目。当前阶段聚焦 Spring 核心基础。

## 技术栈版本 / Version Matrix

| Component | Version | Notes |
|---|---|---|
| Java | 21 (LTS) | Spring Cloud Alibaba 2025.1 要求 Java 21+ |
| Spring Boot | 4.0.5 | 基于 Spring Framework 7.0.6 |
| Spring Framework | 7.0.6 | |
| Spring Cloud | 2025.1 (Oakwood) | 对应 Spring Boot 4.0.x |
| Spring Cloud Alibaba | 2025.1.0.0 | Nacos 3.0 / Sentinel 2.0 / Seata 2.0 |
| Maven | 3.9+ | 构建工具 |

> **Note:** `examples/` 下的示例项目当前基于 Spring Boot 3.5.0 + Java 17，后续将升级至上述版本。

## 文档入口 / Documentation

**[→ 进入文档 docs/README.md](docs/README.md)**

### 核心基础篇 (Phase 1)

IoC & DI / Spring MVC / 自动配置与 Starter / 事务管理

## 示例项目 / Examples

**[→ 示例项目说明 examples/README.md](examples/README.md)**

每个示例项目都是独立可运行的 Spring Boot 项目，配套 Docker Compose 文件。

## 快速开始 / Quick Start

**[→ 详细入门指南 docs/getting-started.md](docs/getting-started.md)**

```bash
# 运行示例项目（核心基础篇无外部中间件依赖）
cd examples/spring-ioc-demo
mvn spring-boot:run
```

## License

MIT License
