# =============================================================================
# Spring Boot Docker 部署指南
# =============================================================================

## 1. 构建 Docker 镜像

### 基本构建
```bash
# 进入项目目录
cd examples/spring-docker-demo

# 构建镜像
docker build -t spring-docker-demo:latest .

# 运行容器
docker run -d -p 8080:8080 --name spring-app spring-docker-demo:latest
```

### 使用 Maven 构建
```bash
# 使用 Spring Boot Maven 插件构建镜像
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=spring-docker-demo:latest

# 或使用 Cloud Native Buildpacks（无需 Dockerfile）
mvn spring-boot:build-image
```

## 2. Docker Compose 部署

### 启动完整环境
```bash
# 启动所有服务（应用 + MySQL + Redis）
docker compose up -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f app

# 停止所有服务
docker compose down
```

### 扩缩容
```bash
# 扩展应用实例
docker compose up -d --scale app=3

# 查看实例分布
docker compose ps
```

## 3. 生产环境部署

### 构建优化
```bash
# 使用分层构建（更快构建速度）
docker build -f Dockerfile.layered -t spring-docker-demo:layered .
```

### 生产环境变量
```bash
# 生产环境运行
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://prod-mysql:3306/prod_db \
  -e SPRING_DATASOURCE_USERNAME=prod_user \
  -e SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD \
  -e JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC" \
  --memory=512m \
  --cpus=1 \
  --restart=unless-stopped \
  spring-docker-demo:latest
```

## 4. Kubernetes 部署

### 创建 Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-docker-demo
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spring-docker-demo
  template:
    metadata:
      labels:
        app: spring-docker-demo
    spec:
      containers:
      - name: spring-docker-demo
        image: spring-docker-demo:latest
        ports:
        - containerPort: 8080
        resources:
          limits:
            memory: "512Mi"
            cpu: "1000m"
          requests:
            memory: "256Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

## 5. 常用命令

### 查看应用日志
```bash
docker logs -f spring-docker-demo

# 查看最近 100 行
docker logs --tail 100 spring-docker-demo
```

### 进入容器调试
```bash
docker exec -it spring-docker-demo /bin/sh
```

### 查看资源使用
```bash
docker stats spring-docker-demo
```

### 重启应用
```bash
docker restart spring-docker-demo
```

## 6. 常见问题

### Q1: 容器启动失败
检查日志：`docker logs spring-docker-demo`

### Q2: 内存不足
调整 JVM 参数：`JAVA_OPTS="-XX:MaxRAMPercentage=75.0"`

### Q3: 健康检查失败
检查端口映射：`docker port spring-docker-demo`
检查应用日志：`docker logs spring-docker-demo`

## 7. 参考链接

- [Spring Boot Docker Guide](https://docs.spring.io/spring-boot/reference/packaging/container-images/dockerfiles.html)
- [Eclipse Temurin Docker Images](https://hub.docker.com/_/eclipse-temurin)
- [Docker Best Practices for Java](https://docs.docker.com/language/java/)
