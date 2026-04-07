package com.example.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Modulith 模块化单体演示主应用
 *
 * 模块结构：
 * - order: 订单模块
 * - inventory: 库存模块
 * - notification: 通知模块
 *
 * 模块间通过 Application Event 进行通信
 */
@SpringBootApplication
public class SpringModulithDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringModulithDemoApplication.class, args);
    }
}
