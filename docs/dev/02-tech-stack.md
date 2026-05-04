# 技术栈

## 版本矩阵

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21 (LTS) | Spring Cloud Alibaba 2025.1 要求 Java 21+ |
| Spring Boot | 4.0.5 | 基于 Spring Framework 7.0.6 |
| Spring Framework | 7.0.6 | |
| Spring Cloud | 2025.1 (Oakwood) | 对应 Spring Boot 4.0.x |
| Spring Cloud Alibaba | 2025.1.0.0 | Nacos 3.0 / Sentinel 2.0 / Seata 2.0 |
| Maven | 3.9+ | 构建工具 |

## 核心依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Data | 随 Boot 版本 | 数据访问抽象 |
| Spring Security | 随 Boot 版本 | 认证授权 |
| MyBatis / MyBatis-Plus | 3.x | ORM 框架 |
| Lombok | 随 Boot 版本 | 代码简化（pom.xml 中 `optional: true`） |
| JUnit 5 | 随 Boot 版本 | 单元测试 |
| TestContainers | 最新稳定版 | 集成测试 |
| SpringDoc OpenAPI | 最新稳定版 | API 文档 |

## 开发环境搭建

### 前置条件

- Java 21+（`java -version`）
- Maven 3.9+（`mvn -version`）
- Docker & Docker Compose（用于启动中间件）
- Git

### 步骤

```bash
# 1. 克隆仓库
git clone <仓库地址>
cd demo_java_spring

# 2. 启动中间件
docker compose -f devops/full-stack-compose.yml up -d

# 3. 运行示例
cd examples/spring-mvc-demo
mvn spring-boot:run

# 4. 访问服务
# http://localhost:8080
```

## 常用命令

```bash
# 运行指定示例项目
cd examples/spring-mvc-demo
mvn spring-boot:run

# 编译
mvn compile

# 打包（跳过测试）
mvn package -DskipTests

# 运行测试
mvn test

# 启动所有中间件
docker compose -f devops/full-stack-compose.yml up -d

# 停止所有中间件
docker compose -f devops/full-stack-compose.yml down
```
