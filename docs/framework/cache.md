# 缓存体系 / Caching Architecture (Spring Cache + Caffeine + Redis)

> 缓存是提升系统性能的第一手段。Spring Cache 提供统一抽象，配合 Caffeine 本地缓存和 Redis 分布式缓存，构建高效的多级缓存体系。

## 1. 概述 / Overview

Spring Cache 是一套缓存抽象（注解 + SPI），不绑定具体实现。通过 `@Cacheable` / `@CachePut` / `@CacheEvict` 注解声明缓存行为，底层可切换 ConcurrentHashMap、Caffeine、Redis、Ehcache 等实现。生产环境通常采用 Caffeine（本地 L1）+ Redis（分布式 L2）的多级缓存架构。

## 2. 核心概念 / Core Concepts

### 2.1 缓存模式对比

| 模式 | 读流程 | 写流程 | 适用场景 |
|------|--------|--------|---------|
| Cache Aside | 先查缓存，miss 则查 DB 并回填 | 先更新 DB，再删除缓存 | 最常用，一致性较好 |
| Read Through | 缓存层自动加载 | 同 Cache Aside | 缓存框架支持 loader |
| Write Through | — | 同时写缓存和 DB | 写入一致性要求高 |
| Write Behind | — | 先写缓存，异步批量写 DB | 高写入吞吐，可能丢数据 |

### 2.2 Spring Cache 注解

| 注解 | 作用 | 说明 |
|------|------|------|
| `@Cacheable` | 查缓存，miss 则执行方法并缓存结果 | 读操作 |
| `@CachePut` | 执行方法并更新缓存 | 写操作后更新缓存 |
| `@CacheEvict` | 删除缓存 | 写操作后清除缓存 |
| `@Caching` | 组合多个缓存操作 | 复杂场景 |
| `@CacheConfig` | 类级别缓存配置 | 统一 cacheNames 等 |
| `@EnableCaching` | 启用缓存 | 配置类上 |

**@Cacheable 关键属性：**

| 属性 | 说明 |
|------|------|
| `cacheNames` / `value` | 缓存名称 |
| `key` | 缓存 key（SpEL 表达式） |
| `condition` | 满足条件才缓存 |
| `unless` | 满足条件不缓存（可引用 `#result`） |
| `cacheManager` | 指定缓存管理器 |
| `sync` | 是否同步（防击穿） |

### 2.3 Caffeine vs Redis 对比

| 特性 | Caffeine (L1) | Redis (L2) |
|------|---------------|------------|
| 位置 | JVM 堆内存 | 独立进程/集群 |
| 延迟 | 纳秒级 | 毫秒级（网络 IO） |
| 容量 | 受 JVM 内存限制 | 可扩展到 TB 级 |
| 多实例一致性 | 各实例独立，不一致 | 所有实例共享 |
| 淘汰策略 | W-TinyLFU（命中率极高） | LRU / LFU / TTL |
| 序列化 | 无需（Java 对象引用） | 需要（JSON/Protobuf） |
| 宕机影响 | 随 JVM 丢失 | 可持久化 |

### 2.4 多级缓存架构

```
请求
 │
 ▼
┌─────────────────┐
│  L1: Caffeine    │  ← 命中率高，纳秒级
│  (JVM 本地缓存)   │
└────────┬────────┘
         │ miss
         ▼
┌─────────────────┐
│  L2: Redis       │  ← 分布式共享，毫秒级
│  (分布式缓存)     │
└────────┬────────┘
         │ miss
         ▼
┌─────────────────┐
│  Database        │  ← 查询后回填 L1 + L2
└─────────────────┘
```

**多级缓存一致性方案：**

| 方案 | 说明 | 复杂度 |
|------|------|--------|
| TTL 过期 | L1 设短 TTL（秒级），L2 设长 TTL（分钟级） | 低 |
| Redis Pub/Sub | 数据变更时发布消息，各实例清除 L1 | 中 |
| Redis Keyspace Notification | 监听 Redis key 过期/删除事件 | 中 |
| Canal + MQ | 监听 DB binlog，异步更新缓存 | 高 |

### 2.5 缓存三大问题

| 问题 | 描述 | 解决方案 |
|------|------|---------|
| 缓存穿透 | 查询不存在的数据，每次都打到 DB | 缓存空值 + 布隆过滤器 |
| 缓存击穿 | 热点 key 过期瞬间大量请求打到 DB | `sync=true` / 分布式锁 / 永不过期 + 异步刷新 |
| 缓存雪崩 | 大量 key 同时过期 | TTL 加随机偏移 / 多级缓存 / 熔断降级 |

## 3. 快速集成 / Quick Start

### 3.1 Maven 依赖

| 依赖 | 说明 |
|------|------|
| `spring-boot-starter-cache` | Spring Cache 抽象 |
| `com.github.ben-manes.caffeine:caffeine` | Caffeine 本地缓存 |
| `spring-boot-starter-data-redis` | Redis 缓存 |

### 3.2 关键配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.cache.type` | 自动检测 | 缓存类型：`caffeine` / `redis` / `simple` |
| `spring.cache.caffeine.spec` | — | Caffeine 规格：`maximumSize=10000,expireAfterWrite=5m` |
| `spring.cache.redis.time-to-live` | — | Redis 缓存 TTL |
| `spring.cache.redis.key-prefix` | — | Redis key 前缀 |
| `spring.cache.redis.use-key-prefix` | `true` | 是否使用前缀 |
| `spring.cache.redis.cache-null-values` | `true` | 是否缓存 null（防穿透） |

## 4. 进阶要点 / Advanced Topics

- **自定义 CacheManager** — 不同缓存名使用不同 TTL 和配置
- **自定义 KeyGenerator** — 替代默认的 SimpleKeyGenerator，避免 key 冲突
- **多级缓存实现** — 自定义 `Cache` 和 `CacheManager` 实现 L1+L2 联动
- **缓存预热** — `ApplicationRunner` 中预加载热点数据
- **缓存监控** — Caffeine 的 `recordStats()` + Micrometer 暴露命中率指标
- **序列化选择** — Redis 缓存建议用 JSON 序列化（可读性）或 Protobuf（性能）
- **大 key 治理** — 避免缓存大对象，拆分或压缩
- **热点 key 发现** — Redis `HOTKEYS` 命令或客户端统计

## 5. 常见问题 / FAQ

| 问题 | 原因 | 解决方案 |
|------|------|---------|
| `@Cacheable` 不生效 | 自调用 / 缺少 `@EnableCaching` | 同 AOP 代理限制 |
| Redis 反序列化失败 | 类结构变更 | 使用 JSON 序列化 + `@type` 或版本化 key |
| 缓存和 DB 不一致 | 先删缓存后更新 DB 的并发问题 | 先更新 DB 再删缓存 + 延迟双删 |
| Caffeine 内存溢出 | maximumSize 设置过大 | 合理设置上限，监控 JVM 内存 |
| 缓存 null 值过多 | 大量无效查询 | 设置短 TTL + 布隆过滤器前置过滤 |

## 6. 示例项目 / Example

> 示例项目位于 [`examples/spring-cache-demo/`](../../examples/spring-cache-demo/)（待创建）
>
> 将演示：Spring Cache + Caffeine、Spring Cache + Redis、多级缓存实现、缓存监控指标

## 7. 参考链接 / References

- [Spring Framework Reference — Cache Abstraction](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [Spring Boot Reference — Caching](https://docs.spring.io/spring-boot/reference/io/caching.html)
- [Caffeine Wiki](https://github.com/ben-manes/caffeine/wiki)
- [Baeldung — Spring Cache](https://www.baeldung.com/spring-cache-tutorial)
