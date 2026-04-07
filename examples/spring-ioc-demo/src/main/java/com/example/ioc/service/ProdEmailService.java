package com.example.ioc.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 生产环境邮件服务
 */
@Service
@Profile("prod")
public class ProdEmailService {

    public ProdEmailService() {
        System.out.println("[ProdEmailService] 生产环境邮件服务初始化");
    }

    public void sendEmail(String to, String subject, String content) {
        System.out.println("[ProdEmailService] 生产环境发送邮件: " + to);
    }
}
