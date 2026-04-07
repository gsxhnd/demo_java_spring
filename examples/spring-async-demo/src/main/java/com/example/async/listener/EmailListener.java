package com.example.async.listener;

import com.example.async.event.EmailSendEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 邮件发送监听器
 */
@Component
@Slf4j
public class EmailListener {

    /**
     * 异步处理邮件发送
     */
    @Async("asyncExecutor")
    @EventListener
    public void handleEmailSendEvent(EmailSendEvent event) {
        log.info("[EmailListener] 开始发送邮件: to={}, subject={}, type={}",
                event.getTo(), event.getSubject(), event.getType());

        try {
            // 模拟邮件发送
            Thread.sleep(1000);

            log.info("[EmailListener] 邮件发送成功: to={}", event.getTo());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[EmailListener] 邮件发送失败: to={}", event.getTo(), e);
        }
    }
}
