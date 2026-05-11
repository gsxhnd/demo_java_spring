# 故障排查

## 常见错误

### Docker 中间件启动失败

**现象**：`docker compose up -d` 后容器立即退出或无法连接。

**解决方案**：

1. 检查端口占用：

   ```bash
   lsof -i :3306  # macOS/Linux
   ```

2. 如端口被占用，修改 compose 文件中的映射端口或停止本地服务
3. 确保 Docker Desktop（macOS/Windows）或 dockerd（Linux）正在运行

---

### Maven 依赖下载缓慢

**解决方案**：在 `~/.m2/settings.xml` 中配置国内镜像：

```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Aliyun Maven Mirror</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

---

### Spring Boot 启动失败：数据库连接被拒绝

**现象**：`Caused by: java.net.ConnectException: Connection refused`

**解决方案**：

1. 确认 Docker 中间件正在运行：

   ```bash
   docker compose -f devops/full-stack-compose.yml ps
   ```

2. 检查 `application.yml` 中的 host/port/username/password 是否与 Docker Compose 配置一致

---

### 多个示例项目端口冲突

**现象**：`Port 8080 is already in use`

**解决方案**：

```bash
# 方式一：指定其他端口
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

# 方式二：先停止占用端口的进程
lsof -ti:8080 | xargs kill
```

---

### Java 版本不匹配

**现象**：`Fatal error compiling: error: invalid target release: 21`

**解决方案**：

```bash
# 检查当前 Java 版本
java -version

# 安装 Java 21
brew install openjdk@21          # macOS
sdk install java 21.0.0-tem     # Linux (sdkman)

# 设置 JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 21)  # macOS
```

## 日志

- **日志位置**：Spring Boot 默认输出到控制台
- **日志级别调整**：

  ```yaml
  logging:
    level:
      root: INFO
      com.example: DEBUG
      org.springframework.web: TRACE
  ```

## 调试模式

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

## 获取帮助

如果以上方法无法解决你的问题：

1. 查看 [常见问题](./07-faq.md)
2. 查看 [技术参考文档](../reference/README.md) 中的对应模块文档
3. 提交 Issue，请附带：操作系统版本、Java 版本、Maven 版本、Docker 版本、完整错误信息和复现步骤
