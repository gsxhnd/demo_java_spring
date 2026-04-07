# Circuit Breaker — 熔断降级 / Circuit Breaker & Rate Limiting

> Resilience4j / Sentinel：服务容错保护、熔断、降级、限流

## 1. 概述 / Overview

在微服务架构中，一个服务的故障可能导致级联失败（雪崩效应）。熔断降级机制在检测到下游服务异常时，快速失败并返回降级响应，保护系统整体可用性。

### 雪崩效应 / Cascading Failure

```
正常状态：                        雪崩状态：
A → B → C → D                   A → B → C → D (故障)
                                      ↑
                                 B 等待 C 超时
                                      ↑
                                 A 等待 B 超时
                                      ↑
                                 线程池耗尽，A 也挂了
```

### 熔断器状态机 / Circuit Breaker State Machine

```
         失败率 > 阈值
┌────────┐          ┌────────┐
│ CLOSED │ ────────→│  OPEN  │
│ (正常)  │          │ (熔断)  │
└────┬───┘          └────┬───┘
     ↑                   │
     │ 探测成功           │ 等待超时
     │                   ▼
     │            ┌────────────┐
     └────────────│ HALF_OPEN  │
                  │ (半开探测)  │
                  └────────────┘
```

| 状态 | 说明 |
|---|---|
| CLOSED | 正常状态，请求正常通过 |
| OPEN | 熔断状态，请求直接失败（快速失败），返回降级响应 |
| HALF_OPEN | 半开状态，放行少量请求探测下游是否恢复 |

---

## 2. 核心概念 / Core Concepts

### Resilience4j vs Sentinel

| 特性 | Resilience4j | Sentinel |
|---|---|---|
| 开发方 | 社区 | Alibaba |
| 定位 | 轻量级容错库 | 流量治理平台 |
| 熔断 | 支持 | 支持 |
| 限流 | 支持（简单） | 支持（丰富：QPS/线程数/热点参数） |
| 降级 | 支持 | 支持 |
| 控制台 | 无（需 Grafana 看指标） | 内置 Dashboard（实时监控） |
| 规则配置 | 代码 / 配置文件 | 代码 / Dashboard / Nacos 动态推送 |
| 集群限流 | 不支持 | 支持 |
| 适用场景 | Spring Cloud 官方推荐，轻量 | 国内大规模微服务，需要可视化管控 |

### Resilience4j 核心模块

| 模块 | 说明 |
|---|---|
| CircuitBreaker | 熔断器 |
| RateLimiter | 限流器 |
| Retry | 重试 |
| Bulkhead | 隔离（信号量/线程池） |
| TimeLimiter | 超时控制 |

### Sentinel 核心概念

| 概念 | 说明 |
|---|---|
| Resource | 资源（被保护的接口/方法） |
| Flow Rule | 流控规则（QPS/线程数） |
| Degrade Rule | 降级规则（慢调用比例/异常比例/异常数） |
| System Rule | 系统规则（CPU/Load/入口 QPS） |
| Authority Rule | 授权规则（黑白名单） |
| Hot Param Rule | 热点参数限流 |

---

## 3. 快速集成 / Quick Start

### Resilience4j

- 依赖：`spring-cloud-starter-circuitbreaker-resilience4j`
- 关键配置：

| 配置 | 说明 |
|---|---|
| `resilience4j.circuitbreaker.instances.<name>.failure-rate-threshold` | 失败率阈值（默认 50%） |
| `resilience4j.circuitbreaker.instances.<name>.slow-call-rate-threshold` | 慢调用率阈值 |
| `resilience4j.circuitbreaker.instances.<name>.wait-duration-in-open-state` | 熔断等待时间 |
| `resilience4j.circuitbreaker.instances.<name>.sliding-window-size` | 滑动窗口大小 |
| `resilience4j.ratelimiter.instances.<name>.limit-for-period` | 每周期允许请求数 |
| `resilience4j.retry.instances.<name>.max-attempts` | 最大重试次数 |
| `resilience4j.bulkhead.instances.<name>.max-concurrent-calls` | 最大并发数 |

### Sentinel

- 依赖：`spring-cloud-starter-alibaba-sentinel`
- 关键配置：

| 配置 | 说明 |
|---|---|
| `spring.cloud.sentinel.transport.dashboard` | Dashboard 地址 |
| `spring.cloud.sentinel.transport.port` | 与 Dashboard 通信端口 |
| `spring.cloud.sentinel.datasource.<name>.nacos` | Nacos 动态规则源 |

---

## 4. 进阶要点 / Advanced Topics

- **Resilience4j + Feign**：`spring.cloud.openfeign.circuitbreaker.enabled=true`，Feign 接口自动熔断
- **Resilience4j + Gateway**：Gateway Filter 中集成 CircuitBreaker，网关层熔断
- **Resilience4j 组合使用**：CircuitBreaker → Retry → RateLimiter → Bulkhead，按顺序组合
- **Sentinel Dashboard**：实时查看 QPS、RT、异常数，在线修改规则
- **Sentinel 规则持久化**：规则推送到 Nacos，重启不丢失
- **Sentinel 热点参数限流**：按参数值限流（如按用户 ID、商品 ID）
- **Sentinel 集群限流**：Token Server 模式，多实例共享限流配额
- **Fallback 降级策略**：返回默认值、缓存数据、友好错误提示

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| 熔断器一直 OPEN | 检查失败率阈值和滑动窗口配置 |
| Sentinel Dashboard 看不到服务 | 需要先有流量触发，Sentinel 是懒加载 |
| Sentinel 规则重启丢失 | 配置 Nacos 数据源持久化规则 |
| Resilience4j 注解不生效 | 确认引入了 AOP 依赖，方法不能是 private |
| 限流粒度不够细 | Sentinel 热点参数限流，或自定义 KeyResolver |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-microservice-demo/`](../../examples/spring-microservice-demo/)（待生成）

## 7. 参考链接 / References

- [Resilience4j 官方文档](https://resilience4j.readme.io/)
- [Spring Cloud CircuitBreaker](https://docs.spring.io/spring-cloud-circuitbreaker/reference/)
- [Sentinel 官方文档](https://sentinelguard.io/zh-cn/)
- [Sentinel GitHub](https://github.com/alibaba/Sentinel)
