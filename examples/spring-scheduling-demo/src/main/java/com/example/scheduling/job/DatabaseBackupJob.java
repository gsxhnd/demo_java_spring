package com.example.scheduling.job;

import com.example.scheduling.service.DataCleanupService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * Quartz Job: 数据库备份任务
 */
@Component
@Slf4j
@DisallowConcurrentExecution
public class DatabaseBackupJob implements Job {

    private final DataCleanupService dataCleanupService;

    public DatabaseBackupJob(DataCleanupService dataCleanupService) {
        this.dataCleanupService = dataCleanupService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("=== Quartz Job 开始: 数据库备份 ===");
        long startTime = System.currentTimeMillis();

        try {
            // 模拟数据库备份
            log.info("开始执行数据库备份...");

            // 备份完成后执行清理
            long deletedSessions = dataCleanupService.cleanupExpiredSessions();
            long deletedLogs = dataCleanupService.cleanupExpiredLogs();

            log.info("数据库备份完成: 删除了 {} 条过期会话，{} 条过期日志",
                    deletedSessions, deletedLogs);

            long duration = System.currentTimeMillis() - startTime;
            log.info("=== Quartz Job 完成: 数据库备份，耗时 {}ms ===", duration);

        } catch (Exception e) {
            log.error("数据库备份任务执行失败", e);
            throw new JobExecutionException("数据库备份任务执行失败", e, false);
        }
    }
}
