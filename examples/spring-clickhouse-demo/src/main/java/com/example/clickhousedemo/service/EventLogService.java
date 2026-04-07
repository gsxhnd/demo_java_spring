package com.example.clickhousedemo.service;

import com.example.clickhousedemo.entity.EventLog;
import com.example.clickhousedemo.repository.EventLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class EventLogService {

    private final EventLogRepository repository;

    public EventLogService(EventLogRepository repository) {
        this.repository = repository;
    }

    public void batchInsert(List<EventLog> events) {
        repository.batchInsert(events);
    }

    public List<EventLog> findByTimeRange(LocalDateTime start, LocalDateTime end) {
        return repository.findByTimeRange(start, end);
    }

    public List<Map<String, Object>> countByEventType() {
        return repository.countByEventType();
    }

    public List<Map<String, Object>> getHourlyStats(String eventType) {
        return repository.getHourlyStats(eventType);
    }
}
