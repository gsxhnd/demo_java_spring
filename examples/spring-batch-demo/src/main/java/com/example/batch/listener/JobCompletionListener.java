package com.example.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Job 执行监听器
 */
@Component
@Slf4j
public class JobCompletionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("=== Job 开始: {} ===", jobExecution.getJobInstance().getJobName());
        log.info("参数: {}", jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String status = jobExecution.getStatus().toString();
        log.info("=== Job 结束: {}, 状态: {} ===",
                jobExecution.getJobInstance().getJobName(), status);

        if (jobExecution.getAllFailureExceptions().isEmpty()) {
            log.info("Job 执行成功");
        } else {
            log.error("Job 执行失败:");
            jobExecution.getAllFailureExceptions().forEach(e -> log.error("  - {}", e.getMessage()));
        }
    }
}
