# Spring 生态学习项目 文档中心

一站式学习 Java Spring Boot + Spring Cloud 全生态。

## 文档导航

| 目录 | 说明 | 适合谁 |
|------|------|--------|
| [dev/](./dev/README.md) | 开发文档：架构设计、技术栈、路线图 | 贡献者、维护者 |
| [usage/](./usage/README.md) | 使用指南：环境搭建、学习路径、故障排查 | 所有学习者 |
| [reference/](./reference/README.md) | 技术参考：各主题的深度技术文档 | 按需查阅 |

## 快速开始

```bash
# 1. 克隆项目
git clone <仓库地址>
cd demo_java_spring

# 2. 启动中间件（按需）
docker compose -f devops/full-stack-compose.yml up -d mysql redis

# 3. 运行示例项目
cd examples/spring-mvc-demo
mvn spring-boot:run
```

详细步骤见 [使用指南 - 快速开始](./usage/01-quick-start.md)。

## 版本矩阵

| 组件 | 版本 |
|------|------|
| Java | 21 (LTS) |
| Spring Boot | 4.0.5 |
| Spring Framework | 7.0.6 |
| Spring Cloud | 2025.1 (Oakwood) |
| Maven | 3.9+ |

## 学习路线

```
核心基础篇 → 数据库篇 → 框架核心篇 → 微服务篇 → 进阶主题篇
```

1. **核心基础**：[IoC & DI](./reference/01-core/01-ioc-di.md) → [Spring MVC](./reference/01-core/02-spring-mvc.md) → [自动配置](./reference/01-core/03-auto-configuration.md) → [事务管理](./reference/01-core/04-transaction.md)
2. **数据库**：[MySQL](./reference/02-database/01-mysql.md) → [Redis](./reference/02-database/03-redis.md) → [MongoDB](./reference/02-database/04-mongodb.md) → 更多
3. **框架核心**：[Security + JWT](./reference/03-framework/05-security-jwt.md) → [AOP](./reference/03-framework/03-aop.md) → [Actuator](./reference/03-framework/01-actuator.md) → 更多
4. **微服务**：[Gateway](./reference/04-microservice/01-gateway.md) → [服务发现](./reference/04-microservice/02-service-discovery.md) → [配置中心](./reference/04-microservice/03-config-center.md) → 更多
5. **进阶主题**：[WebSocket](./reference/05-advanced/01-websocket.md) → [Batch](./reference/05-advanced/02-batch.md) → [WebFlux](./reference/05-advanced/03-webflux.md) → 更多

## 项目结构

```
demo_java_spring/
├── docs/                  ← 文档中心（你在这里）
│   ├── dev/               ← 开发文档
│   ├── usage/             ← 使用指南
│   └── reference/         ← 技术参考
│       ├── core/          ← 核心基础篇
│       ├── database/      ← 数据库篇
│       ├── framework/     ← 框架核心篇
│       ├── microservice/  ← 微服务篇
│       └── advanced/      ← 进阶主题篇
├── examples/              ← 独立可运行的示例项目
└── devops/                ← Docker Compose 中间件编排
```
