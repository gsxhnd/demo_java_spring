package com.example.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 多级缓存配置
 *
 * L1: Caffeine (本地缓存，纳秒级访问)
 * L2: Redis (分布式缓存，毫秒级访问)
 *
 * 多级缓存读取流程: L1 → L2 → DB
 * 多级缓存写入流程: DB → L2 → L1 (或 DB → L2 后删除 L1)
 */
@Configuration
@Slf4j
public class CacheConfig {

    @Value("${cache.caffeine.spec:maximumSize=1000,expireAfterWrite=60s}")
    private String caffeineSpec;

    @Value("${cache.redis.time-to-live:600000}")
    private long redisTtl;

    @Value("${cache.redis.key-prefix:cache:}")
    private String redisKeyPrefix;

    // ========== L1: Caffeine Cache Manager ==========

    /**
     * Caffeine 本地缓存管理器 (L1)
     */
    @Bean(name = "caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .recordStats());  // 开启统计，用于监控命中率
        return cacheManager;
    }

    // ========== L2: Redis Cache Manager ==========

    /**
     * Redis 缓存管理器 (L2)
     */
    @Bean(name = "redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(redisTtl))
                .prefixCacheNameWith(redisKeyPrefix)
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();  // 不缓存 null 值

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }

    // ========== 默认 Cache Manager (用于 @Cacheable 默认使用) ==========

    /**
     * 默认缓存管理器 (这里使用 Redis 作为默认)
     * 可以根据实际需求切换为 Caffeine 或自定义多级缓存
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("初始化 Redis Cache Manager, TTL: {}ms", redisTtl);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(redisTtl))
                .prefixCacheNameWith(redisKeyPrefix)
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("users", config.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("products", config.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration("categories", config.entryTtl(Duration.ofMinutes(30)))
                .transactionAware()
                .build();
    }

    // ========== 自定义 Caffeine 缓存实例 ==========

    /**
     * 自定义用户缓存实例 (用于手动操作)
     */
    @Bean(name = "userLocalCache")
    public Cache<String, Object> userLocalCache() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .recordStats()
                .build();
    }

    /**
     * 自定义产品缓存实例
     */
    @Bean(name = "productLocalCache")
    public Cache<String, Object> productLocalCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .recordStats()
                .build();
    }
}
