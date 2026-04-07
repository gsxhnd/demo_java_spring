# =============================================================================
# Spring Boot Docker 多阶段构建
# =============================================================================

# ===== 阶段 1: 构建阶段 =====
# 使用 JDK 21 镜像进行编译
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 先复制 pom.xml，利用 Docker 缓存加速依赖下载
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# 设置执行权限
RUN chmod +x mvnw

# 下载依赖（离线模式）
RUN ./mvnw dependency:go-offline -B || true

# 复制源代码
COPY src ./src

# 构建应用
RUN ./mvnw package -DskipTests -B

# ===== 阶段 2: 运行阶段 =====
# 使用 JRE 21 Alpine 镜像（更小）
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 创建非 root 用户（安全最佳实践）
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 从构建阶段复制 jar 包
COPY --from=builder /app/target/*.jar app.jar

# 设置文件权限
RUN chown -R appuser:appgroup /app
USER appuser

# 设置时区
ENV TZ=Asia/Shanghai

# JVM 参数配置（容器感知）
# - XX:MaxRAMPercentage: 堆内存占容器内存的百分比（生产推荐 75%）
# - XX:+UseContainerSupport: 启用容器支持（Java 10+ 默认开启）
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/tmp/heapdump.hprof \
               -Djava.security.egd=file:/dev./urandom"

# 健康检查配置
# 使用 Spring Boot Actuator 的 health 端点
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
