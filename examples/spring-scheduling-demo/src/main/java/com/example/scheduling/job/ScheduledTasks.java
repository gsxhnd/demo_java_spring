package com.example.scheduling.job;

import com.example.scheduling.service.DataCleanupService;
import com.example.scheduling.service.ReportService;
import com.example.scheduling.service.SystemMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Scheduled 定时任务示例
 *
 * 触发方式：
 * - fixedRate: 固定频率（ms），上次开始后计时
 * - fixedDelay: 固定延迟（ms），上次结束后计时
 * - cron: Cron 表达式
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final ReportService reportService;
    private final DataCleanupService dataCleanupService;
    private final SystemMonitorService systemMonitorService;

    // ========== 报告生成任务 ==========

    /**
     * 每5分钟生成一次报告统计
     * Cron: "0 */5 * * * ?" = 每5分钟的第0秒
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void generateReportStats() {
        log.info("[Scheduled] 生成报告统计");
        reportService.generateDailyReport();
    }

    // ========== 数据清理任务 ==========

    /**
     * 每30分钟清理一次过期会话
     * fixedRate: 固定频率 30 分钟
     */
    @Scheduled(fixedRate = 1800000)
    public void cleanupExpiredSessions() {
        log.info("[Scheduled] 清理过期会话");
        dataCleanupService.cleanupExpiredSessions();
    }

    /**
     * 每小时清理一次临时文件
     * Cron: "0 0 * * * ?" = 每小时第0分钟第0秒
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupTempFiles() {
        log.info("[Scheduled] 清理临时文件");
        dataCleanupService.cleanupTempFiles();
    }

    /**
     * 每天凌晨3点清理过期日志
     * Cron: "0 0 3 * * ?" = 每天凌晨3点
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredLogs() {
        log.info("[Scheduled] 清理过期日志");
        dataCleanupService.cleanupExpiredLogs();
    }

    /**
     * 每周日凌晨4点清理过期缓存
     * Cron: "0 0 4 ? * SUN" = 每周日
     */
    @Scheduled(cron = "0 0 4 ? * SUN")
    public void cleanupExpiredCache() {
        log.info("[Scheduled] 清理过期缓存");
        dataCleanupService.cleanupExpiredCache();
    }

    // ========== 系统监控任务 ==========

    /**
     * 每分钟检查一次系统健康状态
     * Cron: "0 * * * * ?" = 每分钟
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkSystemHealth() {
        SystemMonitorService.HealthCheckResult result = systemMonitorService.checkSystemHealth();

        // 如果系统不健康，发送告警
        if (!result.healthy()) {
            systemMonitorService.sendAlert(String.format(
                    "系统健康检查异常: CPU=%.1f%%, Memory=%.1f%%, Disk=%.1f%%",
                    result.cpuUsage(), result.memoryUsage(), result.diskUsage()));
        }
    }

    /**
     * 每5分钟收集一次系统指标
     */
    @Scheduled(fixedRate = 300000, initialDelay = 5000)
    public void collectMetrics() {
        log.info("[Scheduled] 收集系统指标");
        systemMonitorService.collectMetrics();
    }

    // ========== 演示任务 ==========

    /**
     * 应用启动后5秒开始执行，每10秒执行一次
     * 演示 fixedRate 和 initialDelay
     */
    @Scheduled(fixedRate = 10000, initialDelay = 5000)
    public void demoTask() {
        log.info("[Scheduled Demo] 演示任务执行: {}", System.currentTimeMillis());
    }
}
