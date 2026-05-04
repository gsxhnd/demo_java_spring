# Redis — 缓存与 KV 存储集成 / Redis Integration

> Spring Boot + Redis：缓存、Session、分布式锁、发布订阅

## 1. 概述 / Overview

Redis 是高性能内存数据结构存储，支持 String、Hash、List、Set、Sorted Set 等多种数据结构。Spring Boot 通过 Spring Data Redis 集成。

### 核心用途

| 场景 | Redis 数据结构 | 说明 |
|---|---|---|
| 缓存 | String / Hash | 减轻数据库压力 |
| Session 共享 | Hash | 分布式 Session |
| 分布式锁 | String + SETNX | 防止并发冲突 |
| 排行榜 | Sorted Set | 实时排名 |
| 计数器 / 限流 | String + INCR | 接口限流 |
| 消息队列 | List / Stream | 轻量级 MQ |
| 发布订阅 | Pub/Sub | 实时通知 |

---

## 2. 核心概念 / Core Concepts

### 客户端选择

| 客户端 | 特点 | 默认 |
|---|---|---|
| **Lettuce** | 基于 Netty，异步/响应式，线程安全 | Spring Boot 默认 |
| Jedis | 同步客户端，连接非线程安全 | 需手动切换 |

### 序列化方式

| 序列化器 | 推荐用途 |
|---|---|
| `StringRedisSerializer` | Key 序列化（推荐） |
| `GenericJackson2JsonRedisSerializer` | Value 序列化（推荐，JSON 可读） |
| `JdkSerializationRedisSerializer` | 默认但不推荐（不可读） |

### 操作模板

| 模板 | 说明 |
|---|---|
| `StringRedisTemplate` | Key/Value 都是 String，最常用 |
| `RedisTemplate<String, Object>` | Value 为任意对象（需配置 JSON 序列化） |

---

## 3. 快速集成 / Quick Start

### Maven 依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

### 配置速查

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: redis123
      database: 0
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 3000ms
```

---

## 4. 进阶要点 / Advanced Topics

- **RedisTemplate 序列化配置**：自定义 `RedisConfig`，Key 用 String、Value 用 JSON
- **Spring Cache 注解**：`@Cacheable` / `@CachePut` / `@CacheEvict`，声明式缓存
- **分布式锁**：`RedisTemplate.opsForValue().setIfAbsent()` 或 Redisson `RLock`
- **缓存穿透/击穿/雪崩**：布隆过滤器、互斥锁、随机过期时间
- **Redis Cluster**：`spring.data.redis.cluster.nodes` 配置集群节点
- **Redis Sentinel**：`spring.data.redis.sentinel` 配置哨兵高可用
- **Pipeline 批量操作**：`redisTemplate.executePipelined()` 减少网络往返
- **Pub/Sub**：`RedisMessageListenerContainer` + `MessageListener`
- **Redis Stream**：类似 Kafka 的消息流，支持消费者组

---

## 5. 常见问题 / FAQ

| 问题 | 解决方案 |
|---|---|
| 存储乱码 | 配置 JSON 序列化器替代默认 JDK 序列化 |
| 缓存与数据库不一致 | Cache Aside 模式：先更新 DB，再删缓存 |
| 连接池耗尽 | 检查 `max-active` 配置，排查连接泄漏 |
| 大 Key 问题 | 拆分大 Hash/List，避免单 Key 超过 10MB |
| 热 Key 问题 | 本地缓存（Caffeine）+ Redis 二级缓存 |

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-redis-demo/`](../../examples/spring-redis-demo/)

包含：RedisConfig 序列化配置、CRUD 操作、Spring Cache 注解缓存、分布式锁示例。

启动依赖：
```bash
cd devops && docker compose -f redis-compose.yml up -d
```

## 7. 参考链接 / References

- [Spring Data Redis 官方文档](https://docs.spring.io/spring-data/redis/reference/)
- [Lettuce 官方文档](https://lettuce.io/)
- [Redisson GitHub](https://github.com/redisson/redisson)
