package com.example.cache.config;

import com.example.cache.entity.Product;
import com.example.cache.entity.User;
import com.example.cache.repository.ProductRepository;
import com.example.cache.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * 数据初始化配置
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(ProductRepository productRepository,
                                       UserRepository userRepository) {
        return args -> {
            // 初始化产品数据
            if (productRepository.count() == 0) {
                productRepository.save(Product.builder()
                        .name("MacBook Pro 14")
                        .description("Apple M3 Pro 芯片，14英寸 Liquid 视网膜 XDR 显示屏")
                        .price(new BigDecimal("14999.00"))
                        .stock(50)
                        .category("Electronics")
                        .active(true)
                        .build());

                productRepository.save(Product.builder()
                        .name("iPhone 15 Pro")
                        .description("A17 Pro 芯片，钛金属设计，Pro 摄像头系统")
                        .price(new BigDecimal("8999.00"))
                        .stock(200)
                        .category("Electronics")
                        .active(true)
                        .build());

                productRepository.save(Product.builder()
                        .name("AirPods Pro 2")
                        .description("自适应音频，个性化空间音频，USB-C 充电")
                        .price(new BigDecimal("1899.00"))
                        .stock(500)
                        .category("Electronics")
                        .active(true)
                        .build());

                productRepository.save(Product.builder()
                        .name("Spring 实战（第5版）")
                        .description("Spring Boot 权威指南")
                        .price(new BigDecimal("89.00"))
                        .stock(1000)
                        .category("Books")
                        .active(true)
                        .build());

                productRepository.save(Product.builder()
                        .name("Clean Code")
                        .description("代码整洁之道")
                        .price(new BigDecimal("79.00"))
                        .stock(800)
                        .category("Books")
                        .active(true)
                        .build());

                log.info("初始化产品数据: {} 个", productRepository.count());
            }

            // 初始化用户数据
            if (userRepository.count() == 0) {
                userRepository.save(User.builder()
                        .username("zhangsan")
                        .password("$2a$10$dummy_hash")
                        .email("zhangsan@example.com")
                        .fullName("张三")
                        .status(User.UserStatus.ACTIVE)
                        .build());

                userRepository.save(User.builder()
                        .username("lisi")
                        .password("$2a$10$dummy_hash")
                        .email("lisi@example.com")
                        .fullName("李四")
                        .status(User.UserStatus.ACTIVE)
                        .build());

                log.info("初始化用户数据: {} 个", userRepository.count());
            }

            log.info("缓存演示数据初始化完成");
        };
    }
}
