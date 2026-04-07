package com.example.clickhousedemo.repository;

import com.example.clickhousedemo.entity.EventLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class EventLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public EventLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<EventLog> rowMapper = (ResultSet rs, int rowNum) -> {
        EventLog log = new EventLog();
        log.setId(rs.getLong("id"));
        log.setEventType(rs.getString("event_type"));
        log.setUserId(rs.getLong("user_id"));
        log.setEventData(rs.getString("event_data"));
        log.setEventTime(rs.getTimestamp("event_time").toLocalDateTime());
        return log;
    };

    // Create the event_log table with MergeTree engine
    public void createTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS event_log (
                    id       UInt64,
                    event_type  String,
                    user_id     UInt64,
                    event_data  String,
                    event_time  DateTime
                ) ENGINE = MergeTree()
                PARTITION BY toYYYYMM(event_time)
                ORDER BY (event_type, event_time)
                """);
    }

    // Batch insert events
    public void batchInsert(List<EventLog> events) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO event_log (id, event_type, user_id, event_data, event_time) VALUES (?, ?, ?, ?, ?)",
                events.stream().map(e -> new Object[]{
                        e.getId(), e.getEventType(), e.getUserId(), e.getEventData(), e.getEventTime()
                }).toList()
        );
    }

    // Query events within a time range
    public List<EventLog> findByTimeRange(LocalDateTime start, LocalDateTime end) {
        return jdbcTemplate.query(
                "SELECT * FROM event_log WHERE event_time >= ? AND event_time <= ? ORDER BY event_time",
                rowMapper, start, end
        );
    }

    // Count events grouped by event type
    public List<Map<String, Object>> countByEventType() {
        return jdbcTemplate.queryForList(
                "SELECT event_type, count() AS cnt FROM event_log GROUP BY event_type ORDER BY cnt DESC"
        );
    }

    // Get hourly statistics for a specific event type
    public List<Map<String, Object>> getHourlyStats(String eventType) {
        return jdbcTemplate.queryForList(
                "SELECT toStartOfHour(event_time) AS hour, count() AS cnt FROM event_log WHERE event_type = ? GROUP BY hour ORDER BY hour",
                eventType
        );
    }
}
