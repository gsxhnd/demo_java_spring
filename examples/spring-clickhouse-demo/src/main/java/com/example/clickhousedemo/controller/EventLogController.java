package com.example.clickhousedemo.controller;

import com.example.clickhousedemo.entity.EventLog;
import com.example.clickhousedemo.service.EventLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventLogController {

    private final EventLogService service;

    public EventLogController(EventLogService service) {
        this.service = service;
    }

    // POST /api/events/batch - batch insert events
    @PostMapping("/batch")
    public Map<String, Object> batchInsert(@RequestBody List<EventLog> events) {
        service.batchInsert(events);
        return Map.of("inserted", events.size());
    }

    // GET /api/events?start=xxx&end=xxx - query by time range
    @GetMapping
    public List<EventLog> findByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return service.findByTimeRange(start, end);
    }

    // GET /api/events/stats - count by event type
    @GetMapping("/stats")
    public List<Map<String, Object>> countByEventType() {
        return service.countByEventType();
    }

    // GET /api/events/hourly?type=xxx - hourly stats for a given event type
    @GetMapping("/hourly")
    public List<Map<String, Object>> getHourlyStats(@RequestParam String type) {
        return service.getHourlyStats(type);
    }
}
