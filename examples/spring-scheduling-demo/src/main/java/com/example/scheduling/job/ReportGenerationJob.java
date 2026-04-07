package com.example.scheduling.job;

import com.example.scheduling.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * Quartz Job: 报告生成任务
 *
 * @DisallowConcurrentExecution 确保上一次任务执行完成后才开始下一次
 * 避免并发执行导致的数据问题
 */
@Component
@Slf4j
@DisallowConcurrentExecution
public class ReportGenerationJob implements Job {

    private final ReportService reportService;

    public ReportGenerationJob(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("=== Quartz Job 开始: 报告生成 ===");
        long startTime = System.currentTimeMillis();

        try {
            // 生成日报
            String dailyReportId = reportService.generateDailyReport();

            // 生成周报（每周一）
            int dayOfWeek = java.time.LocalDate.now().getDayOfWeek().getValue();
            if (dayOfWeek == 1) {
                String weeklyReportId = reportService.generateWeeklyReport();
            }

            // 生成月报（每月1号）
            int dayOfMonth = java.time.LocalDate.now().getDayOfMonth();
            if (dayOfMonth == 1) {
                String monthlyReportId = reportService.generateMonthlyReport();
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("=== Quartz Job 完成: 报告生成，耗时 {}ms ===", duration);

        } catch (Exception e) {
            log.error("报告生成任务执行失败", e);
            throw new JobExecutionException("报告生成任务执行失败", e, false);
        }
    }
}
