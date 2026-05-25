package com.example.security.config;

import org.casbin.jcasbin.main.Enforcer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@EnableConfigurationProperties(CasbinProperties.class)
public class CasbinConfig {

    @Bean
    public Enforcer casbinEnforcer(CasbinProperties properties, ResourceLoader resourceLoader)
            throws IOException {
        Resource modelResource = resourceLoader.getResource(properties.getModelPath());
        Resource policyResource = resourceLoader.getResource(properties.getPolicyPath());
        Path modelFile = Files.createTempFile("casbin-model-", ".conf");
        Path policyFile = Files.createTempFile("casbin-policy-", ".csv");
        Files.writeString(modelFile, modelResource.getContentAsString(StandardCharsets.UTF_8));
        Files.writeString(policyFile, policyResource.getContentAsString(StandardCharsets.UTF_8));
        modelFile.toFile().deleteOnExit();
        policyFile.toFile().deleteOnExit();
        return new Enforcer(modelFile.toString(), policyFile.toString());
    }
}
