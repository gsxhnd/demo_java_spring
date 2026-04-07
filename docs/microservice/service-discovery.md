# Service Discovery — 服务注册与发现 / Service Registration & Discovery

> Nacos / Eureka / Consul 三大注册中心对比与集成

## 1. 概述 / Overview

服务注册与发现是微服务架构的基石。每个微服务启动时向注册中心注册自己的地址，调用方通过注册中心查找目标服务的可用实例，实现动态路由和负载均衡。

### 为什么需要服务发现

```
没有服务发现：                    有服务发现：
                                 
User Service                     User Service
  → http://10.0.1.5:8081           → lb://order-service
  → http://10.0.1.6:8081              │
  → 硬编码地址，扩容需改配置            ▼
                                 ┌──────────────┐
                                 │ Registry     │
                                 │ (Nacos)      │
                                 │ order-service│
                                 │  → 10.0.1.5  │
                                 │  → 10.0.1.6  │
                                 │  → 10.0.1.7  │ ← 自动发现
                                 └──────────────┘
```

---

## 2. 核心概念 / Core Concepts

### 注册发现流程

```
┌─────────────┐    1. Register     ┌──────────────┐
│ Service A   │ ──────────────────→│              │
│ (Provider)  │    2. Heartbeat    │  Registry    │
│             │ ──────────────────→│  Center      │
└─────────────┘                    │  (Nacos)     │
                                   │              │
┌─────────────┐    3. Discover     │              │
│ Service B   │ ──────────────────→│              │
│ (Consumer)  │    4. Instance List│              │
│             │ ←──────────────────│              │
└──────┬──────┘                    └──────────────┘
       │
       │ 5. Load Balance + Call
       ▼
┌─────────────┐
│ Service A   │
│ Instance    │
└─────────────┘
```

### 三大注册中心对比

| 特性 | Nacos | Eureka | Consul |
|---|---|---|---|
| 开发方 | Alibaba | Netflix (已停更) | HashiCorp |
| CAP 模型 | AP + CP（可切换） | AP | CP |
| 健康检查 | TCP/HTTP/MySQL/自定义 | 客户端心跳 | TCP/HTTP/gRPC/脚本 |
| 配置中心 | 内置（Nacos Config） | 无 | 内置 KV Store |
| 控制台 UI | 内置 Web UI | 内置（简单） | 内置 Web UI |
| 多数据中心 | 支持 | 不支持 | 原生支持 |
| 权重路由 | 支持 | 不支持 | 不支持 |
| 命名空间 | 支持（多环境隔离） | 不支持 | 不支持 |
| 推荐程度 | 国内首选 | 已停更，不推荐新项目 | 海外 / 多语言团队 |
| 协议 | HTTP / gRPC | HTTP | HTTP / DNS |

### 关键概念

| 概念 | 说明 |
|---|---|
| Service Name | 服务名称，调用方通过服务名发现实例 |
| Instance | 服务实例（IP + Port） |
| Namespace | 命名空间，隔离不同环境（dev/test/prod） |
| Group | 分组，同一命名空间下的逻辑分组 |
| Cluster | 集群，同一服务的不同机房/区域 |
| Heartbeat | 心跳，实例定期上报存活状态 |
| Health Check | 健康检查，注册中心主动探测实例状态 |
| Ephemeral | 临时实例（心跳失效自动剔除） vs 持久实例 |

---

## 3. 快速集成 / Quick Start

### Nacos

- 依赖：`spring-cloud-starter-alibaba-nacos-discovery`
- 关键配置：`spring.cloud.nacos.discovery.server-addr`、`namespace`、`group`

### Eureka

- 依赖：`spring-cloud-starter-netflix-eureka-client`（客户端）、`spring-cloud-starter-netflix-eureka-server`（服务端）
- 关键配置：`eureka.client.service-url.defaultZone`

### Consul

- 依赖：`spring-cloud-starter-consul-discovery`
- 关键配置：`spring.cloud.consul.host`、`spring.cloud.consul.port`、`spring.cloud.consul.discovery.service-name`

### 通用配置

| 配置 | 说明 |
|---|---|
| `spring.application.name` | 服务名（注册到注册中心的名称） |
| `server.port` | 服务端口 |

---

## 4. 进阶要点 / Advanced Topics

- **Nacos 命名空间隔离**：dev / test / prod 环境使用不同 Namespace，互不干扰
- **Nacos 权重路由**：设置实例权重，实现流量倾斜（灰度发布）
- **Nacos 集群部署**：3 节点 + MySQL 持久化，生产环境必须集群
- **Eureka 自我保护模式**：网络分区时不剔除实例，防止误删（AP 特性）
- **Consul ACL**：访问控制列表，生产环境安全加固
- **Consul DNS 接口**：通过 DNS 协议发现服务，非 Java 服务也能用
- **Spring Cloud LoadBalancer**：替代已废弃的 Ribbon，支持轮询、随机、自定义策略
- **服务元数据 (Metadata)**：注册时附加自定义信息（版本号、机房等），用于路由决策

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| 服务注册成功但调用 404 | 检查服务名是否一致（大小写敏感） |
| Nacos 控制台看不到服务 | 检查 Namespace 和 Group 是否匹配 |
| Eureka 实例不下线 | 自我保护模式，开发环境可关闭 `eureka.server.enable-self-preservation=false` |
| 多网卡注册了错误 IP | 配置 `spring.cloud.inetutils.preferred-networks` 指定网段 |
| Consul 健康检查失败 | 确认服务暴露了 `/actuator/health` 端点 |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-microservice-demo/`](../../examples/spring-microservice-demo/)（待生成）

## 7. 参考链接 / References

- [Nacos 官方文档](https://nacos.io/docs/latest/)
- [Spring Cloud Netflix Eureka](https://docs.spring.io/spring-cloud-netflix/reference/)
- [Spring Cloud Consul](https://docs.spring.io/spring-cloud-consul/reference/)
- [Spring Cloud Alibaba](https://sca.aliyun.com/)
