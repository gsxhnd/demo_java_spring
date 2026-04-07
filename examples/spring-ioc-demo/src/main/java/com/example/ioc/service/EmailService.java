package com.example.ioc.service;

import org.springframework.stereotype.Service;

/**
 * 邮件服务 - 演示 @Lazy 延迟加载
 *
 * 当 UserService 使用 @Lazy 注入此服务时，
 * EmailService 的实例化会延迟到首次使用时
 */
@Service
public class EmailService {

    public EmailService() {
        System.out.println("[EmailService] 构造方法被调用 - 这个日志在首次使用时才会出现（如果使用 @Lazy）");
    }

    public void sendEmail(String to, String subject, String content) {
        System.out.println("[EmailService] 发送邮件: to=" + to + ", subject=" + subject);
    }
}
