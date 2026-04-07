package com.example.async.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮件发送事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendEvent {

    private String to;
    private String subject;
    private String content;
    private EmailType type;

    public enum EmailType {
        WELCOME,           // 欢迎邮件
        ORDER_CONFIRM,     // 订单确认
        PASSWORD_RESET,    // 密码重置
        NOTIFICATION       // 通知邮件
    }
}
