package com.example.jpa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * JPA 审计配置
 * 配置 AuditorAware 实现，自动填充 @CreatedBy 和 @LastModifiedBy
 */
@Configuration
public class JpaAuditingConfig {

    /**
     * AuditorAware 实现
     * 从 SecurityContext 或其他地方获取当前用户名
     * 这里简化为返回系统用户名
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }
}
