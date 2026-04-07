package com.example.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Step 执行监听器
 */
@Component
@Slf4j
public class StepProgressListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("--- Step 开始: {} ---", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("--- Step 结束: {} ---", stepExecution.getStepName());
        log.info("读取: {}, 写入: {}, 跳过: {}, 提交: {}",
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount(),
                stepExecution.getCommitCount());

        if (stepExecution.getFailureExceptions() != null &&
                !stepExecution.getFailureExceptions().isEmpty()) {
            log.warn("Step 执行中有异常:");
            stepExecution.getFailureExceptions()
                    .forEach(e -> log.warn("  - {}", e.getMessage()));
        }

        return stepExecution.getExitStatus();
    }
}
