# 技术栈

## 语言与运行时

- **语言**：Java 21 (LTS)
- **运行时**：OpenJDK 21+ / GraalVM 21+
- **构建工具**：Maven 3.9+
- **包管理器**：Maven Central

## 版本矩阵

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 21 (LTS) | Spring Cloud Alibaba 2025.1 要求 Java 21+ |
| Spring Boot | 4.0.5 | 基于 Spring Framework 7.0.6 |
| Spring Framework | 7.0.6 | |
| Spring Cloud | 2025.1 (Oakwood) | 对应 Spring Boot 4.0.x |
| Spring Cloud Alibaba | 2025.1.0.0 | Nacos 3.0 / Sentinel 2.0 / Seata 2.0 |
| Maven | 3.9+ | 构建工具 |

## 核心依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 4.0.5 | 核心框架 |
| Spring Framework | 7.0.6 | 基础框架 |
| Spring Cloud | 2025.1 (Oakwood) | 微服务框架 |
| Spring Cloud Alibaba | 2025.1.0.0 | 阿里云微服务组件 |
| Spring Data | 随 Boot 版本 | 数据访问抽象 |
| Spring Security | 随 Boot 版本 | 认证授权 |
| MyBatis / MyBatis-Plus | 3.x | ORM 框架 |
| Lombok | 随 Boot 版本 | 代码简化 |
| JUnit 5 | 随 Boot 版本 | 单元测试 |
| TestContainers | 最新稳定版 | 集成测试 |
| SpringDoc OpenAPI | 最新稳定版 | API 文档 |

## 构建工具

- **包管理器**：Maven (pom.xml)
- **编译命令**：`mvn compile`
- **测试命令**：`mvn test`
- **运行命令**：`mvn spring-boot:run`
- **打包命令**：`mvn package -DskipTests`

## 代码规范

- **编码风格**：遵循 Java 标准编码规范，使用 Lombok 简化 POJO
- **命名约定**：Controller/Service/Repository 分层命名，DTO 使用后缀标识
- **包结构**（每个示例项目）：
  ```
  com.example.{module}/
  ├── controller/     # REST 控制器
  ├── service/        # 业务逻辑
  ├── repository/     # 数据访问
  ├── entity/         # 实体类
  ├── dto/            # 数据传输对象
  ├── config/         # 配置类
  └── exception/      # 异常处理
  ```

## 开发环境搭建

### 前置条件

- Java 21+（`java -version`）
- Maven 3.9+（`mvn -version`）
- Docker & Docker Compose（用于启动中间件）
- Git

### 步骤

1. 克隆仓库：`git clone <仓库地址>`
2. 启动中间件：`cd devops && docker compose -f full-stack-compose.yml up -d`
3. 运行示例：`cd examples/spring-mvc-demo && mvn spring-boot:run`
4. 访问服务：浏览器打开 `http://localhost:8080`

## 常用命令

```bash
# 启动所有中间件
cd devops
docker compose -f full-stack-compose.yml up -d

# 运行指定示例项目
cd examples/spring-mysql-demo
mvn spring-boot:run

# 运行测试
mvn test

# 编译项目
mvn compile

# 打包项目
mvn package -DskipTests

# 停止所有中间件
cd devops
docker compose -f full-stack-compose.yml down
```

## 项目结构

```text
spring-ecosystem-learning/
├── README.md              # 项目主文档
├── LICENSE                # MIT
├── docs/              # 文档中心（统一文档体系）
│   ├── dev/               # 开发文档（本目录）
│   ├── wiki/              # 代码描述与设计决策
│   ├── usage/             # 使用指南
│   └── reference/         # 技术参考文档
│       ├── core/          # 核心基础篇
│       ├── database/      # 数据库篇
│       ├── framework/     # 框架核心篇
│       ├── microservice/  # 微服务篇
│       └── advanced/      # 进阶主题篇
├── devops/                # 中间件编排
├── examples/              # 示例项目
│   ├── README.md
│   ├── spring-ioc-demo/
│   ├── spring-mvc-demo/
│   ├── spring-mysql-demo/
│   ├── spring-security-demo/
│   └── ... (21 个项目)
└── .vscode/               # IDE 配置
```
