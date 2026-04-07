package com.example.scheduling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 任务调度演示主应用
 *
 * @EnableScheduling 启用 @Scheduled 定时任务
 * @EnableAsync 启用异步任务支持
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SpringSchedulingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSchedulingDemoApplication.class, args);
    }
}
