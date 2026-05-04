# 快速开始

> 30 秒上手 Spring 生态学习项目

## 前置条件

- Java 21+（推荐 OpenJDK 21 LTS）
- Maven 3.9+
- Docker & Docker Compose
- Git

## 最小示例

```bash
# 1. 克隆项目
git clone <仓库地址>
cd demo_java_spring

# 2. 启动中间件
cd examples/docker-compose
docker compose -f mysql-compose.yml up -d
cd ../..

# 3. 运行第一个示例项目
cd examples/spring-mysql-demo
mvn spring-boot:run
```

预期效果：
- 应用启动在 `http://localhost:8080`
- 可通过 `curl http://localhost:8080/api/products` 访问 REST API
- MySQL 数据库自动创建表和初始数据

## 下一步

- 了解更多安装方式 → [安装指南](./02-installation.md)
- 了解配置选项 → [配置说明](./03-configuration.md)
- 开始日常使用 → [基础使用](./04-basic-usage.md)
