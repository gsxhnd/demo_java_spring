package com.example.mybatis;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class SpringDataMybatisDemoApplication {

    public static void main(String[] args) {
        log.info("========================================");
        log.info("   Spring MyBatis 示例项目 - 启动应用");
        log.info("   - MyBatis 注解方式 (@Select / @Insert / @Update / @Delete)");
        log.info("   - MyBatis XML Mapper (动态 SQL: <if> / <where> / <foreach>)");
        log.info("   - @Param 参数绑定");
        log.info("   - 读写分离 (双 DataSource + SqlSessionFactory)");
        log.info("========================================");
        SpringApplication.run(SpringDataMybatisDemoApplication.class, args);
        log.info("========================================");
        log.info("   Spring Boot 应用启动完成");
        log.info("   API 文档: http://localhost:8080/swagger-ui.html");
        log.info("========================================");
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring MyBatis Demo API")
                        .version("1.0.0")
                        .description("Spring MyBatis 示例项目 API 文档\n\n" +
                                "- 注解 SQL (@Select / @Insert / @Update / @Delete)\n" +
                                "- XML Mapper 动态 SQL (<if> / <where> / <foreach>)\n" +
                                "- @Param 参数绑定\n" +
                                "- 多数据源读写分离")
                        .contact(new Contact()
                                .name("Spring Demo Team")
                                .email("demo@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
