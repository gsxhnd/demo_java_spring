package com.example.multidb;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Slf4j
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.example.multidb.repository.mongo")
@EnableElasticsearchRepositories(basePackages = "com.example.multidb.repository.es")
public class SpringMultiDatabaseDemoApplication {

    public static void main(String[] args) {
        log.info("========================================");
        log.info("   Spring 多数据库示例 - 启动应用");
        log.info("   - Redis (缓存 / 分布式锁)");
        log.info("   - MongoDB (文档 / 用户行为日志)");
        log.info("   - Elasticsearch (全文检索)");
        log.info("   - InfluxDB (时序指标)");
        log.info("========================================");
        SpringApplication.run(SpringMultiDatabaseDemoApplication.class, args);
        log.info("========================================");
        log.info("   应用启动完成");
        log.info("   API 文档: http://localhost:8080/swagger-ui.html");
        log.info("========================================");
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Multi-Database Demo API")
                        .version("1.0.0")
                        .description("单项目集成 Redis / MongoDB / Elasticsearch / InfluxDB\n\n" +
                                "- `/api/redis/**` 缓存与分布式锁\n" +
                                "- `/api/mongo/**` 用户行为日志\n" +
                                "- `/api/es/**` 商品检索\n" +
                                "- `/api/influx/**` 监控指标")
                        .contact(new Contact()
                                .name("Spring Demo Team")
                                .email("demo@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
