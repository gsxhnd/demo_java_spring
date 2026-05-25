package com.example.security;

import com.example.security.config.CasbinProperties;
import com.example.security.config.JwtProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, CasbinProperties.class})
public class SpringSecurityDemoApplication {

    public static void main(String[] args) {
        log.info("========================================");
        log.info("   Spring Security 示例 - 启动应用");
        log.info("   - SecurityFilterChain + JWT");
        log.info("   - UserDetailsService 认证");
        log.info("   - Casbin RBAC 授权");
        log.info("========================================");
        SpringApplication.run(SpringSecurityDemoApplication.class, args);
        log.info("   Swagger: http://localhost:8080/swagger-ui.html");
        log.info("   默认账号: admin/admin123  user/user123");
        log.info("========================================");
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Security Demo API")
                        .version("1.0.0")
                        .description("JWT 无状态认证 + Casbin RBAC 授权"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
