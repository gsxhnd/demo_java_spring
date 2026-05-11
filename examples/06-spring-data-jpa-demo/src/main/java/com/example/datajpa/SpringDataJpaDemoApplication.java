package com.example.datajpa;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Slf4j
@SpringBootApplication
@EnableJpaAuditing
public class SpringDataJpaDemoApplication {

    public static void main(String[] args) {
        log.info("========================================");
        log.info("   Spring Data JPA 示例项目 - 启动应用");
        log.info("   - ORM 与 JPA (@Entity / @Table / @Id / @GeneratedValue)");
        log.info("   - Spring Data JPA (JpaRepository / @Query / Pageable)");
        log.info("   - Entity 审计 (@CreatedDate / @LastModifiedDate)");
        log.info("   - @Enumerated / @Embeddable / @Embedded");
        log.info("   - 事务管理 (@Transactional / propagation / isolation)");
        log.info("   - 读写分离 (双 DataSource + @Primary / @Qualifier)");
        log.info("========================================");
        SpringApplication.run(SpringDataJpaDemoApplication.class, args);
        log.info("========================================");
        log.info("   Spring Boot 应用启动完成");
        log.info("   API 文档: http://localhost:8080/swagger-ui.html");
        log.info("========================================");
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Data JPA Demo API")
                        .version("1.0.0")
                        .description("Spring Data JPA 示例项目 API 文档\n\n" +
                                "- ORM 与 JPA 实体映射\n" +
                                "- Spring Data JPA Repository\n" +
                                "- Entity 审计、枚举、嵌入对象\n" +
                                "- 声明式事务管理\n" +
                                "- 多数据源读写分离")
                        .contact(new Contact()
                                .name("Spring Demo Team")
                                .email("demo@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
