package com.example.business.config;

import com.example.business.entity.Product;
import com.example.business.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final ProductRepository productRepository;

    @Bean
    @Profile("dev")
    CommandLineRunner seedProducts() {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            productRepository.save(Product.builder()
                    .name("Spring Boot 实战")
                    .description("Spring Boot 4 入门与进阶")
                    .price(new BigDecimal("89.90"))
                    .stock(100)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
            productRepository.save(Product.builder()
                    .name("Redis 设计与实现")
                    .description("缓存与高性能架构")
                    .price(new BigDecimal("69.00"))
                    .stock(50)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
            log.info("开发环境示例商品已初始化");
        };
    }
}
