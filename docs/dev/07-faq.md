# 常见问题

## 环境相关

### Q: 我没有安装 Docker，能直接运行示例项目吗？

部分示例项目不依赖中间件（如 `spring-ioc-demo`, `spring-mvc-demo`, `spring-autoconfig-demo`, `spring-transaction-demo` 使用 H2 内存数据库），可以无需 Docker 直接运行。数据库相关的示例项目需要对应中间件，推荐使用 Docker，也可以自行安装本地中间件并修改 `application.yml` 的连接配置。

---

### Q: 必须使用 Java 21 吗？

目标版本为 Java 21（Spring Cloud Alibaba 2025.1 要求）。当前大部分示例代码可运行在 Java 17 上，但建议升级到 Java 21 以获得完整的依赖兼容性。

---

### Q: Maven 和 Gradle 哪个更好？

本项目使用 Maven（3.9+）。Maven 在国内 Java 生态中覆盖率更高，降低学习门槛。如果你更熟悉 Gradle，也可以用 Gradle 构建，但官方只提供 Maven 的 pom.xml。

## 学习相关

### Q: 我是 Spring 零基础，应该从哪里开始？

建议按顺序学习：
1. [IoC & DI](../reference/01-core/01-ioc-di.md) → 理解 IoC 容器
2. [Spring MVC](../reference/01-core/02-spring-mvc.md) → 掌握 Web 开发
3. [自动配置](../reference/01-core/03-auto-configuration.md) → 理解自动配置
4. 然后根据兴趣选择数据库篇或框架核心篇

---

### Q: 文档太多，我只想快速了解某个模块？

每个模块的 `README.md` 索引页列出了该模块的所有文档和一句话摘要，可以快速定位感兴趣的主题。

---

### Q: 示例代码和文档不一致怎么办？

示例代码为准。文档可能因版本迭代而滞后。如果你发现不一致，欢迎提交 Issue 或 PR。

---

### Q: 我需要学完所有模块吗？

不需要。本项目的模块是独立的，可以按需学习。建议核心基础篇必读，其余根据你的工作场景选择。

## 运行相关

### Q: 启动示例项目后，如何访问 API？

示例项目默认运行在 `http://localhost:8080`。具体 API 端点请参考各项目的 README.md 或通过 Swagger UI 查看（如果项目集成了 OpenAPI）。

---

### Q: Docker 镜像拉取太慢怎么办？

配置 Docker 镜像加速器：

```json
// Docker Desktop → Settings → Docker Engine
{
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.xuanyuan.me"
  ]
}
```

---

### Q: 示例项目的数据库表没有自动创建？

检查 `application.yml` 中的 `spring.jpa.hibernate.ddl-auto` 配置：
- `update`：自动更新表结构（推荐开发环境）
- `create`：每次启动重建表（数据会丢失）
- `validate`：只验证不创建
- `none`：不做任何操作

---

### Q: 如何重置数据库数据？

```bash
# 停止并删除容器和卷
docker compose -f devops/full-stack-compose.yml down -v

# 重新启动
docker compose -f devops/full-stack-compose.yml up -d
```

## 贡献相关

### Q: 我可以贡献自己的示例项目吗？

欢迎贡献！请参考 [项目架构](./01-architecture.md) 中的示例项目约定，然后提交 PR。建议先开 Issue 讨论你的想法。

---

### Q: 如何更新某个文档？

在 `docs/` 目录下找到对应文档，直接修改后提交 PR。如果是新增文档，请同步更新该模块的 `README.md` 索引页。
