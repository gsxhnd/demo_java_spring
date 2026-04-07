package com.example.scheduling.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据清理服务
 * 演示数据清理定时任务
 */
@Service
@Slf4j
public class DataCleanupService {

    private final AtomicLong cleanupCount = new AtomicLong(0);
    private final AtomicLong deletedRecordsCount = new AtomicLong(0);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 清理过期会话数据
     */
    public long cleanupExpiredSessions() {
        long count = cleanupCount.incrementAndGet();
        long deleted = (long) (Math.random() * 100);  // 模拟删除数量

        deletedRecordsCount.addAndGet(deleted);

        log.info("清理过期会话 #{}: 删除了 {} 条会话记录", count, deleted);
        return deleted;
    }

    /**
     * 清理临时文件
     */
    public long cleanupTempFiles() {
        long deleted = (long) (Math.random() * 50);  // 模拟删除数量
        log.info("清理临时文件: 删除了 {} 个临时文件", deleted);
        return deleted;
    }

    /**
     * 清理过期日志
     */
    public long cleanupExpiredLogs() {
        long deleted = (long) (Math.random() * 200);  // 模拟删除数量
        log.info("清理过期日志: 删除了 {} 条日志记录", deleted);
        return deleted;
    }

    /**
     * 清理过期缓存
     */
    public long cleanupExpiredCache() {
        long deleted = (long) (Math.random() * 500);  // 模拟删除数量
        log.info("清理过期缓存: 清除了 {} 个缓存键", deleted);
        return deleted;
    }

    /**
     * 清理统计数据
     */
    public CleanupStats getStats() {
        return new CleanupStats(
                cleanupCount.get(),
                deletedRecordsCount.get(),
                LocalDateTime.now().format(formatter)
        );
    }

    /**
     * 清理统计
     */
    public record CleanupStats(long cleanupRuns, long totalDeletedRecords, String lastUpdate) {}
}
