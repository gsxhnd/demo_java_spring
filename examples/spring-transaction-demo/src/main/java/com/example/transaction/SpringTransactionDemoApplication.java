package com.example.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring 事务管理演示主应用
 *
 * @EnableTransactionManagement 启用声明式事务管理
 * (Spring Boot 自动配置已包含此注解，此处显式声明以示强调)
 */
@SpringBootApplication
@EnableTransactionManagement
public class SpringTransactionDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTransactionDemoApplication.class, args);
    }
}
