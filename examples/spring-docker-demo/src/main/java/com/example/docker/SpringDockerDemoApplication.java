package com.example.docker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Docker 部署演示主应用
 *
 * 本项目演示 Spring Boot 应用的 Docker 容器化部署
 */
@SpringBootApplication
public class SpringDockerDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringDockerDemoApplication.class, args);
    }
}
