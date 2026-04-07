package com.example.scheduling.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 调度器配置
 * 配置 @Scheduled 使用的线程池
 */
@Configuration
public class SchedulingConfig {

    /**
     * 配置定时任务线程池
     *
     * 注意：默认情况下 @Scheduled 使用单线程池，
     * 如果有多个定时任务，它们会串行执行。
     * 这里配置了 10 个线程的线程池，允许并行执行。
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("scheduled-");
        scheduler.setErrorHandler(throwable -> {
            // 记录任务执行错误
            System.err.println("定时任务执行错误: " + throwable.getMessage());
        });
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        return scheduler;
    }
}
