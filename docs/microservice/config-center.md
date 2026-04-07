# Config Center — 配置中心 / Configuration Center

> Nacos Config / Spring Cloud Config：集中配置管理、动态刷新、多环境隔离

## 1. 概述 / Overview

配置中心将分散在各微服务中的配置集中管理，支持运行时动态刷新，无需重启服务。

### 为什么需要配置中心

| 痛点 | 配置中心解决方案 |
|---|---|
| 配置散落在各服务的 yml 中 | 集中存储，统一管理 |
| 修改配置需要重启服务 | 动态刷新，实时生效 |
| 不同环境配置混乱 | 多环境隔离（dev/test/prod） |
| 敏感配置（密码）明文存储 | 加密存储 |
| 配置变更无审计 | 变更历史、版本回滚 |

---

## 2. 核心概念 / Core Concepts

### Nacos Config vs Spring Cloud Config

| 特性 | Nacos Config | Spring Cloud Config |
|---|---|---|
| 存储后端 | Nacos 内置（MySQL 持久化） | Git / SVN / JDBC / Vault |
| 动态刷新 | 原生支持（长轮询推送） | 需 Spring Cloud Bus + MQ |
| 控制台 | 内置 Web UI，可视化编辑 | 无 UI（需第三方） |
| 多格式 | properties / yaml / json / xml | properties / yaml |
| 灰度发布 | 支持（Beta 发布） | 不支持 |
| 命名空间 | 支持 | 不支持（用 profile/label 区分） |
| 推荐场景 | 国内微服务（配合 Nacos 注册中心） | 海外 / Git 工作流团队 |

### Nacos Config 配置模型

```
Namespace (命名空间)          ← 环境隔离：dev / test / prod
  └── Group (分组)            ← 逻辑分组：DEFAULT_GROUP
       └── Data ID (配置ID)   ← 配置文件标识
            └── Content       ← 配置内容（yaml/properties）
```

Data ID 命名规则：`${spring.application.name}-${profile}.${file-extension}`

| 示例 Data ID | 说明 |
|---|---|
| `user-service.yml` | 默认配置 |
| `user-service-dev.yml` | 开发环境配置 |
| `user-service-prod.yml` | 生产环境配置 |
| `shared-config.yml` | 共享配置（多服务共用） |

### Spring Cloud Config 配置模型

```
Git Repository
  ├── application.yml          ← 所有服务共享
  ├── user-service.yml         ← user-service 专属
  ├── user-service-dev.yml     ← user-service 开发环境
  └── user-service-prod.yml    ← user-service 生产环境
```

优先级：`user-service-dev.yml` > `user-service.yml` > `application.yml`

---

## 3. 快速集成 / Quick Start

### Nacos Config

- 依赖：`spring-cloud-starter-alibaba-nacos-config`、`spring-cloud-starter-bootstrap`
- 关键配置（`bootstrap.yml`）：

| 配置 | 说明 |
|---|---|
| `spring.cloud.nacos.config.server-addr` | Nacos 地址 |
| `spring.cloud.nacos.config.namespace` | 命名空间 ID |
| `spring.cloud.nacos.config.group` | 分组 |
| `spring.cloud.nacos.config.file-extension` | 配置格式（yaml/properties） |
| `spring.cloud.nacos.config.shared-configs[]` | 共享配置列表 |
| `spring.cloud.nacos.config.refresh-enabled` | 是否启用动态刷新 |

### Spring Cloud Config

- Server 依赖：`spring-cloud-config-server`
- Client 依赖：`spring-cloud-starter-config`、`spring-cloud-starter-bootstrap`
- Server 关键配置：

| 配置 | 说明 |
|---|---|
| `spring.cloud.config.server.git.uri` | Git 仓库地址 |
| `spring.cloud.config.server.git.search-paths` | 搜索路径 |
| `spring.cloud.config.server.git.default-label` | 默认分支 |

---

## 4. 进阶要点 / Advanced Topics

- **动态刷新**：Nacos 原生支持；Spring Cloud Config 需配合 `@RefreshScope` + Spring Cloud Bus（RabbitMQ/Kafka）广播刷新事件
- **`@RefreshScope`**：标记在 Bean 上，配置变更时自动重建 Bean
- **`@ConfigurationProperties`**：绑定配置到 POJO，配合 `@RefreshScope` 实现动态刷新
- **共享配置**：多个服务共用的配置（如数据库连接、Redis 地址）抽取为共享配置
- **配置加密**：Nacos 支持配置加密插件；Spring Cloud Config 支持 `{cipher}` 前缀加密
- **配置灰度发布**：Nacos Beta 发布，先推送到部分实例验证，再全量发布
- **配置版本回滚**：Nacos 内置历史版本管理，一键回滚
- **多配置文件**：一个服务可加载多个 Data ID，按优先级合并

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| 配置不生效 | 检查 Data ID 命名是否匹配（`${name}-${profile}.${ext}`） |
| 动态刷新不生效 | 确认 Bean 加了 `@RefreshScope`，或使用 `@ConfigurationProperties` |
| bootstrap.yml 不加载 | Spring Boot 4.x 推荐使用 `spring.config.import=nacos:` 替代 bootstrap.yml |
| Nacos 配置优先级 | 远程配置 > 本地配置（可通过 `override-none=true` 改变） |
| Config Server 拉取 Git 慢 | 配置 `clone-on-start=true` 启动时预拉取 |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-microservice-demo/`](../../examples/spring-microservice-demo/)（待生成）

## 7. 参考链接 / References

- [Nacos Config 官方文档](https://nacos.io/docs/latest/guide/user/open-api/)
- [Spring Cloud Config 官方文档](https://docs.spring.io/spring-cloud-config/reference/)
- [Spring Cloud Alibaba Config](https://sca.aliyun.com/)
