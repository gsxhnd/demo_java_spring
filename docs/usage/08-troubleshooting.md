# 故障排查

## 常见错误

### Docker 中间件启动失败

**现象**：`docker compose up -d` 后容器立即退出或无法连接。

**原因**：
- 端口被占用（如本地已安装 MySQL 占用了 3306）
- Docker 引擎未运行
- 磁盘空间不足

**解决方案**：

  1. 检查端口占用：
     ```bash
     lsof -i :3306  # macOS/Linux
     netstat -ano | findstr :3306  # Windows
     ```
  2. 如端口被占用，修改 compose 文件中的映射端口或停止本地服务
  3. 确保 Docker Desktop（macOS/Windows）或 dockerd（Linux）正在运行

---

### Maven 依赖下载缓慢

**现象**：`mvn spring-boot:run` 时长时间卡在下载依赖。

**原因**：Maven Central 仓库在国内访问较慢。

**解决方案**：

  在 `~/.m2/settings.xml` 中配置国内镜像：

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

**现象**：
```
Caused by: java.net.ConnectException: Connection refused
```

**原因**：数据库中间件未启动或连接配置不正确。

**解决方案**：

  1. 确认 Docker 中间件正在运行：
     ```bash
     cd devops
     docker compose -f full-stack-compose.yml ps
     ```
  2. 检查 `application.yml` 中的 host/port/username/password 是否与 Docker Compose 配置一致
  3. 如使用 `localhost` 连接，确认中间件在 Docker 中暴露了端口

---

### 多个示例项目端口冲突

**现象**：
```
Port 8080 is already in use
```

**原因**：另一个示例项目已占用 8080 端口。

**解决方案**：

```bash
# 方式一：指定其他端口
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

# 方式二：先停止占用端口的进程
lsof -ti:8080 | xargs kill
```

---

### Java 版本不匹配

**现象**：
```
Fatal error compiling: error: invalid target release: 21
```

**原因**：当前 Java 版本低于项目要求的 Java 21。

**解决方案**：

```bash
# 检查当前 Java 版本
java -version

# 安装 Java 21
# macOS
brew install openjdk@21
# Linux (sdkman)
sdk install java 21.0.0-tem

# 设置 JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 21)  # macOS
export JAVA_HOME=$HOME/.sdkman/candidates/java/21.0.0-tem  # Linux (sdkman)
```

## 日志

- **日志位置**：Spring Boot 默认输出到控制台
- **日志级别调整**：在 `application.yml` 中修改：
  ```yaml
  logging:
    level:
      root: INFO
      com.example: DEBUG  # 项目包 DEBUG 级别
      org.springframework.web: TRACE  # Spring Web 详细日志
  ```
- **查看完整启动日志**：
  ```bash
  mvn spring-boot:run 2>&1 | tee app.log
  ```

## 调试模式

```bash
# 启用 JVM 远程调试
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

## 获取帮助

如果以上方法无法解决你的问题：

1. 查看 [常见问题](./09-faq.md)
2. 查看 [技术参考文档](../reference/00-readme.md) 中的对应模块文档
3. 提交 Issue，请附带以下信息：
   - 操作系统和版本（`uname -a`）
   - Java 版本（`java -version`）
   - Maven 版本（`mvn -version`）
   - Docker 版本（`docker --version`）
   - 完整的错误信息和堆栈跟踪
   - 复现步骤
