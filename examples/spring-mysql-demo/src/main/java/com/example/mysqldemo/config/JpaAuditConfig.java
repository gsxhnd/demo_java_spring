package com.example.mysqldemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Enable JPA auditing for @CreatedDate and @LastModifiedDate
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
}
