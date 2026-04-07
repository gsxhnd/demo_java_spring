package com.example.async.listener;

import com.example.async.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 用户注册监听器
 */
@Component
@Slf4j
public class UserListener {

    /**
     * 处理用户注册事件 - 创建用户画像
     */
    @EventListener
    public void handleUserRegisteredForProfile(UserRegisteredEvent event) {
        log.info("[UserListener-Profile] 处理用户注册: userId={}, 创建用户画像",
                event.getUserId());

        try {
            // 模拟创建用户画像
            Thread.sleep(200);
            log.info("[UserListener-Profile] 用户画像创建完成: userId={}", event.getUserId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
