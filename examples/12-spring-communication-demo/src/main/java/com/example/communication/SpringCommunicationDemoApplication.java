package com.example.communication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;

@SpringBootApplication
@EnableIntegration
public class SpringCommunicationDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCommunicationDemoApplication.class, args);
    }
}
