package com.example.clickhousedemo.config;

import com.example.clickhousedemo.repository.EventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Initializes ClickHouse table on application startup
@Component
public class ClickHouseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ClickHouseInitializer.class);
    private final EventLogRepository repository;

    public ClickHouseInitializer(EventLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing ClickHouse tables...");
        repository.createTable();
        log.info("ClickHouse tables initialized.");
    }
}
