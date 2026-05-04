# 安装指南

## 系统要求

| 项目 | 要求 |
|------|------|
| 操作系统 | macOS / Linux / Windows (WSL2 推荐) |
| Java | OpenJDK 21+ LTS |
| Maven | 3.9+ |
| Docker | Docker Engine 24+ + Docker Compose v2 |
| 磁盘空间 | ~5 GB（含 Docker 镜像和中间件数据） |
| Git | 2.x |

## 安装方式

### 方式一：Git Clone（推荐）

```bash
git clone <仓库地址>
cd demo_java_spring
```

### 方式二：下载 ZIP

从项目主页下载 ZIP 包并解压。

## 环境准备

### 安装 Java 21

**macOS (Homebrew)**：
```bash
brew install openjdk@21
```

**Linux (sdkman)**：
```bash
sdk install java 21.0.0-tem
```

**验证**：
```bash
java -version
# 预期输出：openjdk version "21.0.x" ...
```

### 安装 Maven

**macOS (Homebrew)**：
```bash
brew install maven
```

**Linux (sdkman)**：
```bash
sdk install maven 3.9.9
```

**验证**：
```bash
mvn -version
# 预期输出：Apache Maven 3.9.x, Java version: 21.x ...
```

### 安装 Docker

**macOS**：下载 [Docker Desktop for Mac](https://www.docker.com/products/docker-desktop/)

**Linux**：
```bash
curl -fsSL https://get.docker.com | sh
```

**验证**：
```bash
docker --version
docker compose version
```

## 启动中间件

### 一次性启动所有中间件

```bash
cd devops
docker compose -f full-stack-compose.yml up -d
```

首次启动会拉取镜像，需要 5-10 分钟（取决于网络速度）。

### 按需启动

```bash
# 只启动 MySQL
docker compose -f full-stack-compose.yml up -d mysql

# 只启动 MySQL + Redis
docker compose -f full-stack-compose.yml up -d mysql redis
```

## 安装验证

```bash
# 验证中间件状态
cd devops
docker compose -f full-stack-compose.yml ps

# 运行一个示例项目验证环境
cd ../spring-mvc-demo
mvn spring-boot:run
# 访问 http://localhost:8080/api/hello
```

预期输出：
```
Started SpringMvcDemoApplication in X.XXX seconds
```

## 下一步

安装完成后，请阅读 [配置说明](./03-configuration.md) 了解中间件配置详情。
