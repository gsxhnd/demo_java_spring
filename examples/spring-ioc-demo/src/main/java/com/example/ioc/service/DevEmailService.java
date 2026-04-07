package com.example.ioc.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 开发环境邮件服务
 */
@Service
@Profile({"dev", "default"})
public class DevEmailService {

    public DevEmailService() {
        System.out.println("[DevEmailService] 开发环境邮件服务初始化");
    }

    public void sendEmail(String to, String subject, String content) {
        System.out.println("[DevEmailService] 开发环境发送邮件: to=" + to + ", subject=" + subject + ", content=" + content);
    }
}
