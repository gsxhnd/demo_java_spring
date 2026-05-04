# 快速开始

> 环境准备 + 30 秒上手

## 环境要求

| 工具 | 最低版本 | 用途 |
|------|---------|------|
| Java (JDK) | 21 LTS | 编译运行 Spring Boot 应用 |
| Maven | 3.9+ | 项目构建 |
| Docker | 24+ | 运行中间件（MySQL、Redis 等） |
| Docker Compose | v2 | 编排多个中间件容器 |
| Git | 2.x | 克隆项目 |

验证环境：

```bash
java -version          # openjdk 21.x.x
mvn -version           # Apache Maven 3.9.x
docker --version       # Docker version 24.x+
docker compose version # Docker Compose version v2.x
```

## 安装环境

### Java 21

```bash
# macOS
brew install openjdk@21

# Linux (sdkman)
sdk install java 21.0.0-tem
```

### Maven

```bash
# macOS
brew install maven

# Linux (sdkman)
sdk install maven 3.9.9
```

### Docker

- **macOS**：下载 [Docker Desktop for Mac](https://www.docker.com/products/docker-desktop/)
- **Linux**：`curl -fsSL https://get.docker.com | sh`

## 获取项目

```bash
git clone <仓库地址>
cd demo_java_spring
```

## 最小示例

```bash
# 1. 启动 MySQL 中间件
docker compose -f devops/full-stack-compose.yml up -d mysql

# 2. 运行第一个示例项目
cd examples/spring-mvc-demo
mvn spring-boot:run
```

预期效果：
- 应用启动在 `http://localhost:8080`
- 控制台输出 `Started SpringMvcDemoApplication in X.XXX seconds`

## 启动所有中间件

```bash
docker compose -f devops/full-stack-compose.yml up -d
```

包含的服务：

| 服务 | 端口 |
|------|------|
| MySQL 8.0 | 3306 |
| PostgreSQL 16 | 5432 |
| Redis 7 | 6379 |
| MongoDB 7 | 27017 |
| Elasticsearch 8.15 | 9200 |
| Kibana 8.15 | 5601 |
| ClickHouse | 8123, 9000 |
| InfluxDB 2.7 | 8086 |

按需只启动部分服务：

```bash
docker compose -f devops/full-stack-compose.yml up -d mysql redis
```

## 停止

```bash
# 停止 Spring Boot：Ctrl+C

# 停止中间件
docker compose -f devops/full-stack-compose.yml down
```

## 下一步

- 了解配置选项 → [配置说明](./02-configuration.md)
- 开始学习 → [学习指南](./03-learning-guide.md)
- 查阅技术参考 → [技术参考文档](../reference/README.md)
