package com.example.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * WebSocket + SSE 实时通信演示主应用
 */
@SpringBootApplication
@EnableScheduling
public class SpringWebsocketDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringWebsocketDemoApplication.class, args);
    }
}
