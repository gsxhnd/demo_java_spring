package com.example.scheduling.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 系统监控服务
 * 演示系统监控定时任务
 */
@Service
@Slf4j
public class SystemMonitorService {

    private final AtomicLong monitorCount = new AtomicLong(0);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 检查系统健康状态
     */
    public HealthCheckResult checkSystemHealth() {
        long count = monitorCount.incrementAndGet();

        // 模拟健康检查
        double cpuUsage = Math.random() * 100;
        double memoryUsage = Math.random() * 100;
        double diskUsage = Math.random() * 100;

        boolean healthy = cpuUsage < 80 && memoryUsage < 85 && diskUsage < 90;

        HealthCheckResult result = new HealthCheckResult(
                healthy,
                cpuUsage,
                memoryUsage,
                diskUsage,
                LocalDateTime.now().format(formatter)
        );

        if (healthy) {
            log.debug("系统健康检查 #{}: CPU={:.1f}%, Memory={:.1f}%, Disk={:.1f}%",
                    count, cpuUsage, memoryUsage, diskUsage);
        } else {
            log.warn("系统健康检查 #{}: 发现异常! CPU={:.1f}%, Memory={:.1f}%, Disk={:.1f}%",
                    count, cpuUsage, memoryUsage, diskUsage);
        }

        return result;
    }

    /**
     * 收集系统指标
     */
    public MetricsData collectMetrics() {
        double cpuUsage = Math.random() * 100;
        double memoryUsage = Math.random() * 100;
        long activeThreads = (long) (Math.random() * 50) + 10;
        long requestCount = (long) (Math.random() * 1000);

        return new MetricsData(
                cpuUsage,
                memoryUsage,
                activeThreads,
                requestCount,
                LocalDateTime.now().format(formatter)
        );
    }

    /**
     * 发送健康告警
     */
    public void sendAlert(String message) {
        log.warn("系统告警: {}", message);
        // 实际应用中这里会发送邮件、短信或钉钉消息
    }

    /**
     * 健康检查结果
     */
    public record HealthCheckResult(
            boolean healthy,
            double cpuUsage,
            double memoryUsage,
            double diskUsage,
            String timestamp
    ) {}

    /**
     * 指标数据
     */
    public record MetricsData(
            double cpuUsage,
            double memoryUsage,
            long activeThreads,
            long requestCount,
            String timestamp
    ) {}
}
