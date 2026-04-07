package com.example.scheduling.config;

import com.example.scheduling.job.DatabaseBackupJob;
import com.example.scheduling.job.ReportGenerationJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz Job 配置
 * 定义 Job、Trigger 和 Scheduler
 */
@Configuration
public class QuartzConfig {

    // ========== Job Detail 配置 ==========

    /**
     * 报告生成 Job
     */
    @Bean
    public JobDetail reportGenerationJobDetail() {
        return JobBuilder.newJob(ReportGenerationJob.class)
                .withIdentity("reportGenerationJob", "scheduling")
                .withDescription("每日报告生成任务")
                .storeDurably()  // 即使没有 Trigger 关联也保留 JobDetail
                .requestRecovery(true)  // 集群环境下恢复执行
                .build();
    }

    /**
     * 数据库备份 Job
     */
    @Bean
    public JobDetail databaseBackupJobDetail() {
        return JobBuilder.newJob(DatabaseBackupJob.class)
                .withIdentity("databaseBackupJob", "scheduling")
                .withDescription("数据库备份任务")
                .storeDurably()
                .requestRecovery(true)
                .build();
    }

    // ========== Trigger 配置 ==========

    /**
     * 报告生成触发器 - 每天凌晨 2 点执行
     */
    @Bean
    public Trigger reportGenerationTrigger(JobDetail reportGenerationJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(reportGenerationJobDetail)
                .withIdentity("reportGenerationTrigger", "scheduling")
                .withDescription("每日报告生成触发器")
                // Cron 表达式：秒 分 时 日 月 周
                // "0 0 2 * * ?" = 每天凌晨 2:00:00
                .withSchedule(CronScheduleBuilder
                        .cronSchedule("0 0 2 * * ?")
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }

    /**
     * 数据库备份触发器 - 每周日凌晨 3 点执行
     */
    @Bean
    public Trigger databaseBackupTrigger(JobDetail databaseBackupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(databaseBackupJobDetail)
                .withIdentity("databaseBackupTrigger", "scheduling")
                .withDescription("数据库备份触发器")
                // "0 0 3 ? * SUN" = 每周日 3:00:00
                .withSchedule(CronScheduleBuilder
                        .cronSchedule("0 0 3 ? * SUN")
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }
}
