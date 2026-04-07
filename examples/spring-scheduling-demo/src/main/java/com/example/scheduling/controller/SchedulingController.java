package com.example.scheduling.controller;

import com.example.scheduling.service.DataCleanupService;
import com.example.scheduling.service.ReportService;
import com.example.scheduling.service.SystemMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 调度任务控制器
 * 提供手动触发任务和查看统计的接口
 */
@RestController
@RequestMapping("/api/scheduling")
@RequiredArgsConstructor
public class SchedulingController {

    private final ReportService reportService;
    private final DataCleanupService dataCleanupService;
    private final SystemMonitorService systemMonitorService;

    // ========== 报告操作 ==========

    /**
     * 手动生成日报
     */
    @PostMapping("/reports/daily")
    public ResponseEntity<Map<String, Object>> generateDailyReport() {
        String reportId = reportService.generateDailyReport();
        return ResponseEntity.ok(Map.of(
                "message", "日报生成成功",
                "reportId", reportId
        ));
    }

    /**
     * 手动生成周报
     */
    @PostMapping("/reports/weekly")
    public ResponseEntity<Map<String, Object>> generateWeeklyReport() {
        String reportId = reportService.generateWeeklyReport();
        return ResponseEntity.ok(Map.of(
                "message", "周报生成成功",
                "reportId", reportId
        ));
    }

    /**
     * 获取报告统计
     */
    @GetMapping("/reports/stats")
    public ResponseEntity<ReportService.ReportStats> getReportStats() {
        return ResponseEntity.ok(reportService.getStats());
    }

    // ========== 清理操作 ==========

    /**
     * 清理过期会话
     */
    @PostMapping("/cleanup/sessions")
    public ResponseEntity<Map<String, Object>> cleanupSessions() {
        long deleted = dataCleanupService.cleanupExpiredSessions();
        return ResponseEntity.ok(Map.of(
                "message", "会话清理完成",
                "deletedCount", deleted
        ));
    }

    /**
     * 清理临时文件
     */
    @PostMapping("/cleanup/temp-files")
    public ResponseEntity<Map<String, Object>> cleanupTempFiles() {
        long deleted = dataCleanupService.cleanupTempFiles();
        return ResponseEntity.ok(Map.of(
                "message", "临时文件清理完成",
                "deletedCount", deleted
        ));
    }

    /**
     * 清理过期日志
     */
    @PostMapping("/cleanup/logs")
    public ResponseEntity<Map<String, Object>> cleanupLogs() {
        long deleted = dataCleanupService.cleanupExpiredLogs();
        return ResponseEntity.ok(Map.of(
                "message", "日志清理完成",
                "deletedCount", deleted
        ));
    }

    /**
     * 获取清理统计
     */
    @GetMapping("/cleanup/stats")
    public ResponseEntity<DataCleanupService.CleanupStats> getCleanupStats() {
        return ResponseEntity.ok(dataCleanupService.getStats());
    }

    // ========== 监控操作 ==========

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<SystemMonitorService.HealthCheckResult> checkHealth() {
        return ResponseEntity.ok(systemMonitorService.checkSystemHealth());
    }

    /**
     * 获取系统指标
     */
    @GetMapping("/metrics")
    public ResponseEntity<SystemMonitorService.MetricsData> getMetrics() {
        return ResponseEntity.ok(systemMonitorService.collectMetrics());
    }

    /**
     * Cron 表达式说明
     */
    @GetMapping("/cron-help")
    public ResponseEntity<Map<String, String>> getCronHelp() {
        Map<String, String> help = new HashMap<>();
        help.put("every_5_minutes", "0 */5 * * * ?");
        help.put("every_hour", "0 0 * * * ?");
        help.put("daily_2am", "0 0 2 * * ?");
        help.put("weekly_sunday_3am", "0 0 3 ? * SUN");
        help.put("monthly_1st_1am", "0 0 1 1 * ?");
        help.put("every_30_seconds", "0/30 * * * * ?");
        help.put("workday_9_to_18", "0 0 9-18 * * MON-FRI");
        return ResponseEntity.ok(help);
    }
}
