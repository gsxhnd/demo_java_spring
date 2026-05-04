# 进阶使用

> Spring 生态学习项目的高级功能与进阶技巧

## 自定义中间件配置

当 Docker 默认配置不满足需求时，可以修改 Docker Compose 文件。

### 修改 MySQL 配置

编辑 `devops/mysql-compose.yml`：

```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: your_strong_password
      MYSQL_DATABASE: my_custom_db
    ports:
      - "3307:3306"  # 修改宿主机端口
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init-sql:/docker-entrypoint-initdb.d  # 自定义初始化 SQL
```

### 添加新中间件

在 `devops/` 创建新的 compose 文件：

```yaml
# rabbitmq-compose.yml
services:
  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin123
```

## 调试示例项目

### 启用 Debug 日志

在 `application.yml` 中添加：

```yaml
logging:
  level:
    com.example: DEBUG
    org.springframework: INFO
```

### 远程调试

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

IDE 中配置 Remote JVM Debug，连接到 `localhost:5005`。

## 创建自己的示例项目

### 项目模板

```bash
# 1. 使用 Spring Initializr 快速创建
# 访问 https://start.spring.io/ 或使用 CLI
spring init \
  --group=com.example \
  --artifact=spring-my-demo \
  --name=spring-my-demo \
  --dependencies=web,data-jpa,mysql,lombok \
  --java-version=21 \
  spring-my-demo

# 2. 放入 examples/ 目录
mv spring-my-demo examples/

# 3. 遵循项目约定的包结构
mkdir -p src/main/java/com/example/my/{controller,service,repository,entity,dto,config}
```

### 遵循项目约定

1. 使用 `spring-boot-starter-parent:4.0.5` 作为 parent
2. Java 版本设为 21
3. 使用与 Docker Compose 一致的连接配置
4. 包含基础的单元测试
5. 编写项目级 README.md 说明功能和使用方式

## 同时运行多个示例项目

```bash
# 终端 1 - 启动 MySQL 示例（端口 8080）
cd examples/spring-mysql-demo
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"

# 终端 2 - 启动 Redis 示例（端口 8081）
cd examples/spring-redis-demo
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

# 终端 3 - 启动 Security 示例（端口 8082）
cd examples/spring-security-demo
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
```

## 容器化部署示例项目

每个示例项目都可通过 Docker 运行：

```bash
cd examples/spring-mysql-demo

# 构建镜像
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=spring-mysql-demo

# 运行容器
docker run -p 8080:8080 --network host spring-mysql-demo
```

或参考 `examples/spring-docker-demo/` 查看完整的 Dockerfile 和 docker-compose 示例。

## 下一步

- 遇到问题 → [故障排查](./08-troubleshooting.md)
- 有疑问 → [常见问题](./09-faq.md)
- 查阅技术细节 → [技术参考文档](../reference/00-readme.md)
