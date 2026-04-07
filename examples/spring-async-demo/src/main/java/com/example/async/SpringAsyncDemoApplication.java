package com.example.async;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步处理演示主应用
 *
 * @EnableAsync 启用 @Async 异步方法支持
 */
@SpringBootApplication
@EnableAsync
public class SpringAsyncDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAsyncDemoApplication.class, args);
    }
}
