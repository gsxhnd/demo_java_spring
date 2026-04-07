package com.example.autoconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring Boot 自动配置演示主应用
 *
 * @SpringBootApplication 包含:
 * - @SpringBootConfiguration: 本质是 @Configuration
 * - @EnableAutoConfiguration: 启用自动配置
 * - @ComponentScan: 组件扫描
 */
@SpringBootApplication
@ConfigurationPropertiesScan  // 扫描 @ConfigurationProperties 注解的类
public class SpringAutoconfigDemoApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(SpringAutoconfigDemoApplication.class, args);

        // 打印激活的 Profile
        System.out.println("\n========== 自动配置演示 ==========");
        System.out.println("激活的 Profile: " + String.join(", ",
                context.getEnvironment().getActiveProfiles()));

        context.close();
    }
}
