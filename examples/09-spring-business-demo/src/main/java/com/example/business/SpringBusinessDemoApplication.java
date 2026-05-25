package com.example.business;

import com.example.business.config.AppProperties;
import com.example.business.config.AsyncProperties;
import com.example.business.config.CacheProperties;
import com.example.business.config.ScheduledProperties;
import com.example.business.config.StorageProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({
        AppProperties.class,
        CacheProperties.class,
        AsyncProperties.class,
        StorageProperties.class,
        ScheduledProperties.class
})
public class SpringBusinessDemoApplication {

    public static void main(String[] args) {
        log.info("========================================");
        log.info("   Spring 业务能力示例 - 启动应用");
        log.info("   - Service 层业务封装");
        log.info("   - @ConfigurationProperties + Profile");
        log.info("   - @Async / @Scheduled");
        log.info("   - @Cacheable + Redis");
        log.info("   - 文件上传/下载");
        log.info("========================================");
        SpringApplication.run(SpringBusinessDemoApplication.class, args);
        log.info("========================================");
        log.info("   API 文档: http://localhost:8080/swagger-ui.html");
        log.info("========================================");
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Business Demo API")
                        .version("1.0.0")
                        .description("业务能力示例：Service、配置、异步、缓存、文件")
                        .contact(new Contact().name("Spring Demo Team").email("demo@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
