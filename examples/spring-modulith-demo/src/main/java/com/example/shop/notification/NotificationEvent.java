package com.example.shop.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private String recipient;
    private String subject;
    private String content;
    private NotificationType type;
    private LocalDateTime timestamp;

    public enum NotificationType {
        EMAIL,
        SMS,
        PUSH
    }
}
