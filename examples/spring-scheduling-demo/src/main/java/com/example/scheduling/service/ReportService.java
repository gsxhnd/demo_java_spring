package com.example.scheduling.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 报告服务
 * 演示各种定时任务场景
 */
@Service
@Slf4j
public class ReportService {

    private final AtomicLong reportCount = new AtomicLong(0);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成日报
     */
    public String generateDailyReport() {
        long count = reportCount.incrementAndGet();
        String reportId = "DR-" + System.currentTimeMillis();
        log.info("生成日报 #{}: {}", count, reportId);
        log.debug("日报内容：统计今日访问量、订单量、收入等指标");
        return reportId;
    }

    /**
     * 生成周报
     */
    public String generateWeeklyReport() {
        long count = reportCount.incrementAndGet();
        String reportId = "WR-" + System.currentTimeMillis();
        log.info("生成周报 #{}: {}", count, reportId);
        log.debug("周报内容：统计本周数据趋势、环比增长率等");
        return reportId;
    }

    /**
     * 生成月报
     */
    public String generateMonthlyReport() {
        long count = reportCount.incrementAndGet();
        String reportId = "MR-" + System.currentTimeMillis();
        log.info("生成月报 #{}: {}", count, reportId);
        log.debug("月报内容：汇总本月核心业务指标");
        return reportId;
    }

    /**
     * 获取报告统计
     */
    public ReportStats getStats() {
        return new ReportStats(
                reportCount.get(),
                LocalDateTime.now().format(formatter)
        );
    }

    /**
     * 报告统计
     */
    public record ReportStats(long totalReports, String lastUpdate) {}
}
