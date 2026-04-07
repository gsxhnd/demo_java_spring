package com.example.transaction.repository;

import com.example.transaction.model.OperationLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志仓储层
 */
@Repository
public class OperationLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public OperationLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<OperationLog> ROW_MAPPER = (rs, rowNum) -> OperationLog.builder()
            .id(rs.getLong("id"))
            .operation(rs.getString("operation"))
            .entityType(rs.getString("entity_type"))
            .entityId(rs.getLong("entity_id"))
            .details(rs.getString("details"))
            .createTime(rs.getTimestamp("create_time").toLocalDateTime())
            .build();

    public void initTable() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS operation_log (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                operation VARCHAR(50) NOT NULL,
                entity_type VARCHAR(50),
                entity_id BIGINT,
                details TEXT,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
    }

    public void save(OperationLog log) {
        jdbcTemplate.update("""
            INSERT INTO operation_log (operation, entity_type, entity_id, details, create_time)
            VALUES (?, ?, ?, ?, ?)
        """, log.getOperation(), log.getEntityType(),
                log.getEntityId(), log.getDetails(), LocalDateTime.now());
    }

    public List<OperationLog> findAll() {
        return jdbcTemplate.query("SELECT * FROM operation_log ORDER BY create_time DESC", ROW_MAPPER);
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM operation_log");
    }
}
