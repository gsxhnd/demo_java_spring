---
title: Java Spring 缓存
created: 2026-05-08 22:29:19
category: Java-Spring
tags:
  - Java
  - Spring
  - Cache
  - Caffeine
  - Redis
  - 缓存
---

<!-- markdownlint-disable MD025 -->

# Java Spring 缓存

## 为什么要学缓存

在 Part 5 中我们学习了如何使用 Repository 访问数据库，通过 JPA 或 MyBatis 完成了数据的读写。但随着请求量的上升，你会注意到一个现象：很多查询是重复的 — 首页的热门商品列表、配置表里的系统参数、用户权限信息... 每秒钟都要查数据库，但结果几乎不变。数据库查询是相对昂贵的操作（网络往返 + SQL 解析 + 磁盘 IO），当 QPS 从 10 涨到 1000 时，这些重复查询会成为性能瓶颈。

缓存是解决这个问题的标准方案：把频繁访问且变化不大的数据暂存在内存中，下次请求直接拿缓存，不用再打数据库。Spring Cache 提供了声明式的缓存抽象，让你用注解就能实现缓存逻辑，不用手写 `if (cache.has(key))` 这种样板代码。

## 核心概念

### Spring Cache 是什么

Spring Cache 是 Spring 提供的一套缓存抽象层。它通过 `@Cacheable`、`@CacheEvict`、`@CachePut` 等注解，让开发者在方法级别声明缓存行为，而非手写缓存操作代码。

**换个说法：** 手工缓存就像你每次去图书馆都要自己查索引、找书架、取书、登记借阅卡。Spring Cache 就像图书馆的自动借阅系统 — 你告诉系统"我要这本书"（调用方法），系统自动检查是否有人已经借出来了（查缓存），有就直接给你（返回缓存），没有就去书库拿（执行方法），同时登记入库（放入缓存）。

### 为什么需要 Spring Cache

**痛点场景：** 在 Service 层手写缓存逻辑会污染业务代码：

```java
// 没有缓存抽象时，需要手写这些：
public Product getProduct(Long id) {
    String cacheKey = "product:" + id;
    Product cached = cacheManager.get(cacheKey);
    if (cached != null) return cached;

    Product product = productRepo.findById(id).orElse(null);
    if (product != null) {
        cacheManager.set(cacheKey, product, 300, TimeUnit.SECONDS);
    }
    return product;
}
```

每个需要缓存的方法都要重复这个 `get-key → check-null → query → set-cache` 模式，而且更换缓存实现（Caffeine 换成 Redis）需要改所有手写代码。

**设计动机：** Spring Cache 通过 AOP 拦截方法调用，自动在方法执行前查缓存、执行后写缓存。业务方法里只有纯粹的业务逻辑。更换缓存提供者只需修改配置，代码零改动。

### 没有 Spring Cache 会怎样

**困境：** 手写缓存逻辑散落在各处，加入过期、淘汰、缓存穿透保护等逻辑后代码膨胀严重。换一个缓存中间件（从本地缓存换成 Redis）需要改几十处代码。缓存键命名不一致，有的是 `"product_" + id`，有的是 `"product:" + id`，难以统一管理。

**有了 Spring Cache 之后：** 三个注解覆盖 90% 的缓存场景。底层实现用哪个（Caffeine、Redis、Ehcache）是配置的事，业务代码不感知。

## 概念深入解释

### 核心注解

| 注解 | 行为 | 触发时机 |
|------|------|----------|
| `@Cacheable` | 查缓存，有则跳过方法执行，无则执行方法并把结果放入缓存 | 方法**执行前**查缓存 |
| `@CacheEvict` | 从缓存中移除一条或多条数据 | 方法**执行后**（默认）清缓存 |
| `@CachePut` | 不查缓存，**总是执行方法**，用返回值更新缓存 | 方法**执行后**更新缓存 |
| `@Caching` | 组合多个缓存注解 | 需要多种缓存行为同时生效时 |

### @Cacheable 详解

```java
@Service
public class ProductService {

    @Cacheable(value = "products", key = "#id")
    public Product getProduct(Long id) {
        return productRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found"));
    }
}
```

- `value` / `cacheNames` — 缓存名称（命名空间），同一个缓存名下 key 唯一
- `key` — 缓存键，支持 SpEL 表达式。`#id` 表示用方法参数 `id` 的值
- `condition` — 条件缓存，如 `condition = "#id > 100"` 表示 id 大于 100 才缓存
- `unless` — 条件不缓存，如 `unless = "#result == null"` 表示结果为 null 时不缓存

**SpEL 常用写法：**

| 表达式 | 含义 |
|--------|------|
| `#id` | 用参数 `id` 的值 |
| `#user.username` | 用参数 `user` 对象的 `username` 属性 |
| `#result` | 用方法的返回值（仅 `@CacheEvict` 和 `@CachePut` 可用） |
| `'keyPrefix:' + #id` | 拼接字符串 |

### @CacheEvict 详解

```java
@CacheEvict(value = "products", key = "#id")
public void updateProduct(Long id, ProductDTO dto) {
    // 更新数据库后，清除对应缓存
}

// 清除整个命名空间下所有缓存
@CacheEvict(value = "products", allEntries = true)
public void reloadAllProducts() {
}
```

- `beforeInvocation = true` — 在方法执行**前**清除缓存（默认是方法执行后）
- `allEntries = true` — 清除该缓存名下所有条目

### @CachePut 详解

```java
@CachePut(value = "products", key = "#result.id")
public Product createProduct(CreateProductRequest request) {
    Product product = new Product();
    // 设置字段...
    return productRepo.save(product);
}
```

`@CachePut` 的典型场景是新增操作 — 新创建的数据直接放入缓存，下次查询直接命中。注意它的 `key` 通常用 `#result.xxx` 引用返回值。

### 缓存架构分层

```
请求 → Controller → Service (@Cacheable)
                         │
                         ├── 缓存命中 → 直接返回
                         │
                         └── 缓存未命中 → 执行方法 → Repository → DB
                                                          │
                                                          └── 结果放入缓存 → 返回
```

### 缓存提供者对比

| 提供者 | 类型 | 特点 | 适用场景 |
|--------|------|------|----------|
| Caffeine | 本地缓存（JVM 内） | 高性能、近零延迟、W-TinyLFU 淘汰算法 | 单实例、数据量小、对延迟敏感的缓存 |
| Redis | 分布式缓存 | 多实例共享、持久化、丰富的数据结构 | 多实例部署、需要共享状态、大数据量 |
| Ehcache | 本地/分布式 | 老牌方案、支持堆外内存 | 需要本地缓存且堆外存储的场景 |

### 常见问题与应对

**缓存穿透：** 查询一个不存在的数据（id=-1），因为结果为空所以不会缓存，每次请求都穿透到数据库。应对：即使结果为 null 也缓存一个短暂的标记值（`unless = "#result == null"` 配合空值缓存，或使用布隆过滤器）。

**缓存击穿：** 一个热点 key 刚好过期，同时大量请求涌入，全部打到数据库。应对：设置热点数据永不过期，或在缓存失效时加互斥锁，只让一个线程去查数据库。

**缓存雪崩：** 大量缓存同时过期，所有请求集中打到数据库。应对：给过期时间加随机偏移量（如基础 300 秒 + 随机 0-60 秒），避免同时过期。

## 核心要点

1. **读多用 @Cacheable，更新/删除用 @CacheEvict：** 两个注解覆盖最常见的缓存读写场景。
2. **缓存 key 命名规范统一：** 在项目级别统一定义 key 前缀（如 `#service:method:#param`），避免不同模块的 key 冲突。
3. **生产环境显式配置 Caffeine 或 Redis：** 不要依赖 Spring Boot 的默认 `ConcurrentMap` 实现（无过期、无容量限制、堆内存无限增长）。
4. **`@Cacheable` 方法内部不要互相调用：** 和 `@Async` 一样，AOP 代理不拦截类内部的 `this.method()` 调用。
5. **缓存空值防止穿透：** 对可能返回 null 的查询，设置短暂过期时间并缓存空值标记。

## 常见误区

- **默认使用 ConcurrentMap 作为缓存提供者，运行一段时间后 OOM。** Spring Boot 在没有显式引入缓存库时，默认用 `ConcurrentMap` 实现 — 没有过期时间，没有容量限制，缓存数据永远不会被清除。生产环境必须引入 Caffeine 或 Redis 作为缓存提供者。
- **`@CacheEvict` 忘记设置 key，不清除任何缓存。** `@CacheEvict(value = "products")` 没有指定 `key` 时，Spring 默认清除整个命名空间 — 如果你期望的是清除特定条目，需要用 `key = "#id"` 明确指定。反过来，如果用了 `key` 却忘记设置，根本清不掉。
- **在 @Cacheable 方法内部调用同类另一个 @Cacheable 方法，第二个方法不走缓存。** 又是 AOP 代理问题。self-invocation 绕过代理，缓存注解失效。拆到不同 Bean 中解决。
- **缓存和数据库一致性问题处理不当。** 直接删除缓存（`@CacheEvict`）比更新缓存更简单可靠，尤其是并发场景下。先更新数据库再删缓存是最常见的模式。不要反过来（先删缓存再更新数据库），中间窗口期可能读到脏数据。
- **对所有查询不分场景都加缓存。** 高频查询、变化不频繁的数据才适合缓存。如果数据每秒钟都在变（实时股价），缓存在有和没有之间只是多了一层复杂度。缓存能提升性能的前提是"命中率高"。

## 与其他概念的关联

- **前置：** [Java Spring AOP](./08_Java%20Spring%20AOP.md) -- Spring Cache 基于 AOP 代理拦截方法调用
- **前置：** [Java Spring Repository](./25_Java%20Spring%20Repository.md) -- 缓存通常加在查询数据库的方法上
- **前置：** [Java Spring 事务管理](./26_Java%20Spring%20事务管理.md) -- 缓存操作和事务操作的边界需要仔细设计
- **并行：** [Java Spring Service 层](./30_Java%20Spring%20Service%20层.md) -- `@Cacheable` 通常标注在 Service 层方法上
- **并行：** [Java Spring 多数据库](./28_Java%20Spring%20多数据库.md) -- Redis 既可作为数据库也可作为缓存提供者
- **后续：** [Java Spring Cloud 熔断限流](../Spring_Cloud/Java Spring Cloud 熔断限流.md) -- 缓存是防止故障扩散的第一道防线
