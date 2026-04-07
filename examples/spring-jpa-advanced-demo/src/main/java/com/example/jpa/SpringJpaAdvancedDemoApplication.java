package com.example.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 进阶特性演示主应用
 *
 * @EnableJpaAuditing 启用 JPA 审计功能（@CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy）
 */
@SpringBootApplication
@EnableJpaAuditing
public class SpringJpaAdvancedDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringJpaAdvancedDemoApplication.class, args);
    }
}
