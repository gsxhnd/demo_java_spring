# 示例代码组织模块 (examples)

> 独立可运行的 Spring Boot 示例项目集合，配套 Docker Compose 中间件编排

## 设计决策

### 为什么需要这个模块

示例代码是学习材料从"知道"到"会用"的关键桥梁。与文档分离的独立可运行项目让开发者可以快速验证概念，而无需从头搭建环境。

### 为什么这么设计

- **选择了**：每个示例项目为独立的 Maven 项目，使用统一的包结构、命名约定和版本管理
- **而不是**：将所有示例放在一个多模块 Maven 项目中
- **原因**：独立项目确保每个示例可单独运行学习；统一约定降低跨示例的认知负担

## 关键类型与接口

### 项目命名约定

`spring-{topic}-demo`，如：
- `spring-ioc-demo` — IoC 容器
- `spring-mysql-demo` — MySQL 集成
- `spring-security-demo` — Security + JWT
- `spring-microservice-demo` — 微服务综合示例（多模块）

### 统一包结构

```
com.example.{topic}/
├── controller/     # @RestController / @ControllerAdvice
├── service/        # @Service 业务逻辑
├── repository/     # @Repository 数据访问
├── entity/         # @Entity 实体类
├── dto/            # DTO / Request / Response
├── config/         # @Configuration 配置类
├── exception/      # 全局异常处理
└── {Topic}DemoApplication.java  # @SpringBootApplication 入口
```

### Maven 坐标约定

- **groupId**：`com.example`
- **artifactId**：`spring-{topic}-demo`
- **parent**：`spring-boot-starter-parent:4.0.5`
- **java.version**：`21`

## 模块结构

```text
examples/
├── README.md                    # 示例项目总览
├── spring-ioc-demo/             # 核心基础
├── spring-mvc-demo/
├── spring-autoconfig-demo/
├── spring-transaction-demo/
├── spring-mysql-demo/           # 数据库
├── spring-redis-demo/
├── spring-mongodb-demo/
├── spring-es-demo/
├── spring-clickhouse-demo/
├── spring-influxdb-demo/
├── spring-security-demo/        # 框架核心
├── spring-jpa-advanced-demo/
├── spring-cache-demo/
├── spring-scheduling-demo/
├── spring-async-demo/
├── spring-file-demo/
├── spring-microservice-demo/    # 微服务（多模块）
├── spring-websocket-demo/       # 进阶
├── spring-batch-demo/
├── spring-webflux-demo/
├── spring-modulith-demo/
└── spring-docker-demo/
```

## 与其他模块的关系

### 依赖

- **docs/***：每个示例项目对应一组文档

### 被依赖

- 无（代码终点）

## 注意事项

- 示例项目的 Maven wrapper 建议提交到仓库，确保无 Maven 环境的开发者也能构建
- Docker Compose 文件中的密码仅为开发环境默认值，文档中需注明生产环境必须修改
- 示例项目的 `application.yml` 需包含中文注释说明配置项含义
- 每个示例项目的 README.md 需包含：功能说明、前置条件、运行步骤、API 端点（如适用）
- 微服务示例的端口规划需在文档中明确列出，避免本地运行时端口冲突
- 部分示例项目标记为"待生成"，需在 examples/README.md 中明确标注状态
