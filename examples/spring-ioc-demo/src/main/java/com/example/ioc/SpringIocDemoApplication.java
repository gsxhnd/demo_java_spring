package com.example.ioc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring IoC 演示主应用
 *
 * @SpringBootApplication = @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan
 * - @SpringBootConfiguration: 本质是 @Configuration
 * - @EnableAutoConfiguration: 触发自动配置
 * - @ComponentScan: 默认扫描主类所在包及其子包
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example.ioc")
public class SpringIocDemoApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(SpringIocDemoApplication.class, args);

        // 演示：通过 ApplicationContext 获取 Bean
        System.out.println("\n========== IoC 容器演示 ==========");
        demonstrateBeanRetrieval(context);

        context.close();
    }

    private static void demonstrateBeanRetrieval(org.springframework.context.ApplicationContext context) {
        // 1. 按类型获取 Bean
        var userService = context.getBean(com.example.ioc.service.UserService.class);
        System.out.println("获取 UserService: " + userService);
        System.out.println("UserService 依赖的 UserRepository: " + userService.getUserRepository());

        // 2. 查看所有注册的 Bean 名称
        System.out.println("\n注册的 Bean 数量: " + context.getBeanDefinitionCount());
        System.out.println("Bean 名称列表:");
        for (String name : context.getBeanDefinitionNames()) {
            System.out.println("  - " + name);
        }
    }
}
