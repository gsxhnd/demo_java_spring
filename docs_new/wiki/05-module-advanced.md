# 进阶主题模块 (advanced)

> 高级 Spring 特性：实时通信、批处理、响应式编程、模块化单体、容器化部署

## 设计决策

### 为什么需要这个模块

这些主题并非每个项目都会用到，但在特定场景下是不可替代的解决方案。将它们单独归为进阶模块，避免初学者在学习核心内容时被高级主题干扰。

### 为什么这么设计

- **选择了**：将这些主题独立为进阶模块，放在学习路径的最后阶段
- **而不是**：将它们分散到数据库篇或框架核心篇
- **原因**：这些主题需要扎实的 Spring 基础才能理解，且使用场景相对特定

## 关键类型与接口

| 子主题 | 示例项目 | 核心技术 |
|--------|----------|----------|
| WebSocket / SSE | spring-websocket-demo | WebSocket, STOMP, SseEmitter |
| Spring Batch | spring-batch-demo | Job, Step, ItemReader, ItemWriter |
| Spring WebFlux | spring-webflux-demo | Reactor, Mono/Flux, R2DBC |
| Spring Modulith | spring-modulith-demo | ApplicationModule, Event Publication |
| Docker 部署 | spring-docker-demo | Dockerfile, 多阶段构建, JVM 调优 |

## 模块结构

```text
docs/advanced/
├── README.md           # 进阶主题索引
├── websocket.md         # WebSocket / SSE 实时通信
├── batch.md             # Spring Batch 批处理
├── webflux.md           # Spring WebFlux 响应式
├── modulith.md          # Spring Modulith 模块化单体
└── docker-deploy.md     # Docker 容器化部署

examples/
├── spring-websocket-demo/
├── spring-batch-demo/
├── spring-webflux-demo/
├── spring-modulith-demo/
└── spring-docker-demo/
```

## 与其他模块的关系

### 依赖

- **核心基础模块**：WebSocket/Batch/WebFlux 均基于 Spring Boot
- **数据库模块**：Batch 依赖数据库存储 Job 元数据；WebFlux 使用 R2DBC
- **微服务模块**：Docker 部署适用于所有类型项目

### 被依赖

- 无（顶层模块）

### 依赖关系图

```text
进阶主题 (advanced)
  ↑ 基于
  ├── 核心基础 (core)
  ├── 数据库 (database)     → 存储 Job 元数据、R2DBC
  └── 微服务 (microservice) → 容器化部署目标
```

## 注意事项

- WebFlux 的响应式编程模型与传统 Servlet 模型差异巨大，需要从"思维方式"层面解释
- WebFlux 与传统 MVC 不能混用，文档需要明确这一点
- Spring Batch 的 Job 重启、跳过策略、事务边界是生产环境重点
- WebSocket 的连接管理、心跳机制、断线重连需要在示例中体现
- Modulith 是 Spring 较新的模块化方案，适合"先单体后拆分"的演进策略
- Docker 多阶段构建可以有效减小镜像体积，文档应给出优化前后对比
