package com.example.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.casbin")
public class CasbinProperties {

    private String modelPath = "classpath:casbin/model.conf";
    private String policyPath = "classpath:casbin/policy.csv";
}
