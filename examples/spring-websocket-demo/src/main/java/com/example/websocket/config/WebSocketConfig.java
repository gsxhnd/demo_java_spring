package com.example.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP 配置
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置消息代理前缀
        // /topic - 广播消息（发布-订阅模式）
        // /queue - 点对点消息
        registry.enableSimpleBroker("/topic", "/queue");

        // 应用前缀 - 客户端发送消息的目的地以此开头
        // 消息会路由到 @MessageMapping 注解的方法
        registry.setApplicationDestinationPrefixes("/app");

        // 用户目的地前缀（用于点对点消息）
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 STOMP 端点，客户端通过该端点建立 WebSocket 连接
        registry.addEndpoint("/ws")
                // 允许所有来源（生产环境应限制）
                .setAllowedOriginPatterns("*")
                // 启用 SockJS 作为 WebSocket 的降级方案
                .withSockJS();

        // 也可以不使用 SockJS
        registry.addEndpoint("/ws/raw")
                .setAllowedOriginPatterns("*");
    }
}
