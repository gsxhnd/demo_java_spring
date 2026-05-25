package com.example.testing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SpringTestingDemoApplication {

    public static void main(String[] args) {
        log.info("========================================");
        log.info("   Spring 测试示例 - 启动应用");
        log.info("   运行测试: mvn test");
        log.info("========================================");
        SpringApplication.run(SpringTestingDemoApplication.class, args);
    }
}
