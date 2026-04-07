package com.example.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Spring Cache 多级缓存演示主应用
 *
 * @EnableCaching 启用 Spring Cache 抽象
 */
@SpringBootApplication
@EnableCaching
public class SpringCacheDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCacheDemoApplication.class, args);
    }
}
