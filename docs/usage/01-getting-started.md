# 快速开始

> 30 秒上手 Spring 生态学习项目

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
java -version    # openjdk 21.x.x
mvn -version     # Apache Maven 3.9.x
docker --version # Docker version 24.x+
docker compose version  # Docker Compose version v2.x
```

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

## 验证

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 或直接访问业务接口（具体路径参考各项目 README）
```

## 停止

```bash
# 停止 Spring Boot：Ctrl+C

# 停止 MySQL 容器
cd examples/docker-compose
docker compose -f mysql-compose.yml down
```

## 启动所有中间件

如果你想一次性启动所有数据库和中间件：

```bash
cd examples/docker-compose
docker compose -f full-stack-compose.yml up -d
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

也可以按需只启动部分服务：

```bash
# 只启动 MySQL 和 Redis
docker compose -f full-stack-compose.yml up -d mysql redis
```

## 项目结构

```
demo_java_spring/
├── docs/                      ← 文档中心
│   ├── dev/                       ← 开发文档
│   ├── wiki/                      ← 代码描述
│   ├── usage/                     ← 使用指南（你正在看的）
│   └── reference/                 ← 技术参考文档
│       ├── core/                  ← 核心基础篇
│       ├── database/              ← 数据库篇
│       ├── framework/             ← 框架核心篇
│       ├── microservice/          ← 微服务篇
│       └── advanced/              ← 进阶主题篇
│
├── examples/                      ← 示例项目（独立可运行）
│   ├── docker-compose/            ← 中间件 Docker Compose 文件
│   ├── spring-mysql-demo/         ← MySQL + JPA + MyBatis-Plus
│   ├── spring-redis-demo/         ← Redis + Spring Cache
│   └── ...
│
└── README.md
```

## 下一步

- 了解更多安装方式 → [安装指南](./02-installation.md)
- 了解配置选项 → [配置说明](./03-configuration.md)
- 开始日常使用 → [基础使用](./04-basic-usage.md)
- 查阅技术参考 → [技术参考文档](../reference/00-readme.md)
