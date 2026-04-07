package com.example.ioc.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 条件配置演示
 *
 * 演示各种 @Conditional 条件注解的用法
 */
@Configuration
public class ConditionalConfig {

    /**
     * 条件：当 classpath 中存在指定类时注册
     * 典型用途：检测到某依赖后才注册相关 Bean
     */
    @Bean
    @ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
    public String jacksonAvailableMessage() {
        System.out.println("[ConditionalConfig] ObjectMapper 在 classpath 中，注册的 Bean");
        return "Jackson is available";
    }

    /**
     * 条件：当容器中不存在指定类型的 Bean 时才注册
     * 典型用途：用户自定义 Bean 优先，自动配置兜底
     */
    @Bean
    @ConditionalOnMissingBean(name = "customizedService")
    public String defaultService() {
        System.out.println("[ConditionalConfig] 没有 customizedService，注册默认 Bean");
        return "Default Service";
    }

    /**
     * 条件：根据配置属性决定是否注册
     * 典型用途：功能开关
     */
    @Bean
    @ConditionalOnProperty(name = "app.feature.enabled", havingValue = "true", matchIfMissing = false)
    public String featureEnabledMessage() {
        System.out.println("[ConditionalConfig] app.feature.enabled=true，注册功能 Bean");
        return "Feature is enabled";
    }
}
