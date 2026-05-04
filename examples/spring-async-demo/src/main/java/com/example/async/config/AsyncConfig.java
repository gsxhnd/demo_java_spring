package com.example.async.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务执行器配置
 *
 * 可以配置多个不同的线程池，分别用于不同类型的任务
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    /**
     * MDC 上下文传递装饰器
     *
     * 确保 MDC（如 traceId）在异步线程中也能使用
     */
    @Bean(name = "mdcTaskDecorator")
    public TaskDecorator mdcTaskDecorator() {
        return runnable -> {
            var contextMap = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        };
    }

    /**
     * 默认异步任务执行器
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor(TaskDecorator mdcTaskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("async-exec-");

        // 拒绝策略：日志记录并丢弃
        executor.setRejectedExecutionHandler((r, e) -> {
            System.err.println("异步任务被拒绝执行: " + r.toString());
        });

        // 等待任务完成后关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // 应用 MDC 装饰器
        executor.setTaskDecorator(mdcTaskDecorator);

        executor.initialize();
        return executor;
    }

    /**
     * IO 密集型任务执行器（更大的线程池）
     */
    @Bean(name = "ioExecutor")
    public Executor ioExecutor(TaskDecorator mdcTaskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(16);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("io-exec-");
        executor.setTaskDecorator(mdcTaskDecorator);
        executor.initialize();
        return executor;
    }

    /**
     * CPU 密集型任务执行器（较小的线程池）
     */
    @Bean(name = "cpuExecutor")
    public Executor cpuExecutor(TaskDecorator mdcTaskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // CPU 密集型：核心线程数 = CPU 核心数 + 1
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() + 1);
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() + 1);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("cpu-exec-");
        executor.setTaskDecorator(mdcTaskDecorator);
        executor.initialize();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return asyncExecutor(mdcTaskDecorator());
    }
}
