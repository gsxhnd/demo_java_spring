package com.example.transaction.config;

import com.example.transaction.service.AccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 应用初始化配置
 */
@Configuration
public class AppInitConfig {

    @Bean
    public CommandLineRunner initData(AccountService accountService) {
        return args -> {
            System.out.println("========== 初始化测试数据 ==========");
            accountService.initTestData();
            System.out.println("===================================");
        };
    }
}
