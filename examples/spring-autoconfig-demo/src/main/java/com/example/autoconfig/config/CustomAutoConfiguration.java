package com.example.autoconfig.config;

import com.example.autoconfig.properties.AppProperties;
import com.example.autoconfig.service.GreetingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义自动配置类
 *
 * 演示 @Configuration + @Conditional 系列注解实现自动配置
 *
 * 自动配置的核心思想：
 * 1. 检测条件是否满足（@ConditionalOnClass, @ConditionalOnProperty 等）
 * 2. 如果满足，创建并注册 Bean
 * 3. 如果不满足，跳过此配置
 *
 * 自动配置加载顺序：
 * 1. 用户自定义的 @Configuration（最高优先级）
 * 2. @EnableAutoConfiguration 中的自动配置类
 * 3. Spring Boot 内置的自动配置
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class CustomAutoConfiguration {

    /**
     * 条件：当 classpath 中存在特定类时才注册
     *
     * 典型场景：只有引入某个依赖时才注册相关 Bean
     */
    @Bean
    @ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
    public String classpathCheckMessage() {
        System.out.println("[AutoConfiguration] ObjectMapper 在 classpath 中");
        return "Jackson is available";
    }

    /**
     * 条件：当容器中不存在该类型 Bean 时才注册
     *
     * 典型场景：用户自定义 Bean 优先，自动配置兜底
     */
    @Bean
    @ConditionalOnMissingBean(GreetingService.class)
    public GreetingService defaultGreetingService(AppProperties properties) {
        System.out.println("[AutoConfiguration] 注册默认 GreetingService");
        return new GreetingService(properties.getGreeting().getMessage());
    }

    /**
     * 条件：根据配置属性值决定是否注册
     *
     * 典型场景：功能开关
     */
    @Bean
    @ConditionalOnProperty(name = "app.feature.enabled", havingValue = "true", matchIfMissing = false)
    public String featureEnabledMessage() {
        System.out.println("[AutoConfiguration] app.feature.enabled=true，启用功能");
        return "Feature is enabled";
    }

    /**
     * 条件：当指定的配置属性存在且不为 false 时启用
     * matchIfMissing = true 表示配置缺失时也算满足条件
     */
    @Bean
    @ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true", matchIfMissing = true)
    public String cacheEnabledMessage() {
        System.out.println("[AutoConfiguration] 缓存已启用");
        return "Cache is enabled";
    }
}
