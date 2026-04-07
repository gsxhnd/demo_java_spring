# Docker 部署 Java 应用 / Docker Deployment for Java Apps

> Dockerfile 多阶段构建, JVM 容器调优, docker-compose 集成, 镜像瘦身, 健康检查

## 1. 概述 / Overview

将 Spring Boot 应用容器化部署是现代 Java 项目的标准实践。本文覆盖从 Dockerfile 编写到生产环境部署的完整流程。

### 部署方式对比

| 方式 | 适用场景 | 优点 | 缺点 |
|---|---|---|---|
| `java -jar` 直接运行 | 开发/测试 | 简单 | 环境不一致、难以管理 |
| **Docker 容器** | 测试/生产 | 环境一致、易于编排 | 需要 Docker 运行时 |
| Kubernetes | 大规模生产 | 自动扩缩容、自愈 | 运维复杂度高 |
| Cloud Native Buildpacks | CI/CD | 无需 Dockerfile | 灵活性较低 |

---

## 2. 核心概念 / Core Concepts

### 容器化 Java 应用的关键考量

```
┌─────────────────────────────────────────────┐
│              Docker Container                │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │            JVM (Java 21)               │  │
│  │                                        │  │
│  │  ┌──────────────────────────────────┐  │  │
│  │  │     Spring Boot Application      │  │  │
│  │  │                                  │  │  │
│  │  │  Heap    │  Non-Heap  │ Threads  │  │  │
│  │  └──────────────────────────────────┘  │  │
│  └────────────────────────────────────────┘  │
│                                              │
│  cgroup 内存限制 ← Docker --memory           │
│  cgroup CPU 限制 ← Docker --cpus             │
└─────────────────────────────────────────────┘
```

### JVM 容器感知

从 Java 10 开始，JVM 能自动感知容器的 cgroup 资源限制。Java 21 对此支持已非常成熟：

| JVM 参数 | 说明 | 推荐值 |
|---|---|---|
| `-XX:MaxRAMPercentage` | 堆内存占容器内存的百分比 | `75.0`（默认 25%） |
| `-XX:InitialRAMPercentage` | 初始堆内存百分比 | `50.0` |
| `-XX:+UseContainerSupport` | 启用容器感知（Java 10+ 默认开启） | 默认 |
| `-XX:ActiveProcessorCount` | 手动指定 CPU 核数 | 按需设置 |

---

## 3. 快速开始 / Quick Start

### 3.1 基础 Dockerfile（多阶段构建）

```dockerfile
# ===== 阶段 1: 构建 =====
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 先复制 pom.xml，利用 Docker 缓存加速依赖下载
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# 复制源码并构建
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ===== 阶段 2: 运行 =====
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 从构建阶段复制 jar
COPY --from=builder /app/target/*.jar app.jar

# 设置文件权限
RUN chown -R appuser:appgroup /app
USER appuser

# JVM 参数
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 3.2 构建与运行

```bash
# 构建镜像
docker build -t my-spring-app:latest .

# 运行容器
docker run -d \
  --name my-app \
  -p 8080:8080 \
  -m 512m \
  -e SPRING_PROFILES_ACTIVE=prod \
  my-spring-app:latest
```

### 3.3 docker-compose 集成应用 + 中间件

```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/mydb
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=root123
    deploy:
      resources:
        limits:
          memory: 512M
          cpus: "1.0"
    depends_on:
      mysql:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "wget", "-qO-", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 3s
      start-period: 40s
      retries: 3

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: mydb
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql-data:
```

---

## 4. 进阶用法 / Advanced Usage

### 4.1 Spring Boot 分层 JAR（Layered JAR）

Spring Boot 支持将 JAR 分层，充分利用 Docker 缓存：

```dockerfile
# ===== 阶段 1: 构建 =====
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests -B

# 解压分层 JAR
RUN java -Djarmode=tools -jar target/*.jar extract --layers --destination extracted

# ===== 阶段 2: 运行 =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 按变化频率从低到高复制各层（利用缓存）
COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./

RUN chown -R appuser:appgroup /app
USER appuser

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
```

分层优势：依赖层（dependencies）很少变化，Docker 会缓存该层，只有 application 层在代码变更时重建。

### 4.2 Cloud Native Buildpacks（无需 Dockerfile）

```bash
# Spring Boot Maven 插件内置 Buildpacks 支持
mvn spring-boot:build-image \
  -Dspring-boot.build-image.imageName=my-spring-app:latest
```

### 4.3 JVM 调优参数模板

```dockerfile
ENV JAVA_OPTS="\
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0 \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/heapdump.hprof \
  -Djava.security.egd=file:/dev/./urandom"
```

### 4.4 镜像瘦身技巧

| 技巧 | 效果 |
|---|---|
| 使用 `alpine` 基础镜像 | JRE 镜像约 180MB vs 标准 400MB+ |
| 多阶段构建 | 最终镜像不含 JDK、Maven、源码 |
| 分层 JAR | 增量构建更快 |
| `.dockerignore` | 排除无关文件 |
| jlink 自定义 JRE | 可压缩到 80MB 以下 |

`.dockerignore` 示例：

```
target/
.git/
.idea/
*.iml
.mvn/wrapper/maven-wrapper.jar
docs/
README.md
```

### 4.5 jlink 自定义最小 JRE

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS jre-builder

# 分析应用依赖的模块
RUN jdeps --ignore-missing-deps --multi-release 21 \
    --print-module-deps app.jar > modules.txt

# 构建最小 JRE
RUN jlink \
    --add-modules $(cat modules.txt) \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=zip-6 \
    --output /custom-jre

FROM alpine:3.20
COPY --from=jre-builder /custom-jre /opt/java
ENV PATH="/opt/java/bin:$PATH"
COPY app.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### 4.6 生产环境注意事项

| 项目 | 建议 |
|---|---|
| 非 root 用户 | 始终使用 `USER appuser` 运行 |
| 健康检查 | 配置 `HEALTHCHECK` + Actuator `/health` |
| 日志 | 输出到 stdout/stderr，由 Docker/K8s 收集 |
| 时区 | `ENV TZ=Asia/Shanghai` 或挂载 `/etc/localtime` |
| 信号处理 | 使用 `exec` 形式 ENTRYPOINT，确保 SIGTERM 正确传递 |
| 敏感信息 | 通过环境变量或 Secret 注入，不要写入镜像 |
| 镜像标签 | 不要使用 `latest`，使用 Git SHA 或语义化版本 |

---

## 5. 常见问题 / FAQ

### Q1: 容器内 JVM 占用内存超过限制被 OOM Kill

JVM 默认只使用容器内存的 25% 作为堆。设置 `-XX:MaxRAMPercentage=75.0` 让 JVM 合理利用容器内存。同时预留 25% 给非堆内存（Metaspace、线程栈、NIO Buffer 等）。

### Q2: 容器启动很慢

- 使用 Spring Boot 的 Lazy Initialization：`spring.main.lazy-initialization=true`
- 考虑 GraalVM Native Image（启动时间 < 100ms）
- 使用 CDS（Class Data Sharing）加速类加载

### Q3: 时区不对，日志时间错误

```dockerfile
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata
```

或在 `application.yml` 中：

```yaml
spring:
  jackson:
    time-zone: Asia/Shanghai
```

### Q4: 如何在容器内调试？

```bash
# 开启远程调试端口
docker run -p 8080:8080 -p 5005:5005 \
  -e JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
  my-spring-app:latest
```

### Q5: Docker 构建缓存失效，每次都重新下载依赖

确保 Dockerfile 中先复制 `pom.xml` 并下载依赖，再复制源码。参见 3.1 节的多阶段构建示例。

---

## 6. 示例项目 / Example

- 各示例项目的 Docker 部署 → [`examples/docker-compose/`](../../examples/docker-compose/)
- 完整 Docker 部署示例 → [`examples/spring-docker-demo/`](../../examples/spring-docker-demo/)（待创建）

---

## 7. 参考资料 / References

- [Spring Boot Docker Guide](https://docs.spring.io/spring-boot/reference/packaging/container-images/dockerfiles.html)
- [Eclipse Temurin Docker Images](https://hub.docker.com/_/eclipse-temurin)
- [Cloud Native Buildpacks](https://buildpacks.io/)
- [Docker Best Practices for Java](https://docs.docker.com/language/java/)
