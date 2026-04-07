package com.example.transaction.repository;

import com.example.transaction.model.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单仓储层
 */
@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Order> ROW_MAPPER = (rs, rowNum) -> Order.builder()
            .id(rs.getLong("id"))
            .orderNo(rs.getString("order_no"))
            .userId(rs.getLong("user_id"))
            .productName(rs.getString("product_name"))
            .quantity(rs.getInt("quantity"))
            .amount(rs.getBigDecimal("amount"))
            .status(rs.getString("status"))
            .createTime(rs.getTimestamp("create_time").toLocalDateTime())
            .updateTime(rs.getTimestamp("update_time").toLocalDateTime())
            .build();

    public void initTable() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS orders (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                order_no VARCHAR(50) UNIQUE NOT NULL,
                user_id BIGINT NOT NULL,
                product_name VARCHAR(200) NOT NULL,
                quantity INT NOT NULL,
                amount DECIMAL(19,2) NOT NULL,
                status VARCHAR(20) DEFAULT 'PENDING',
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """);
    }

    public Order save(Order order) {
        if (order.getId() == null) {
            jdbcTemplate.update("""
                INSERT INTO orders (order_no, user_id, product_name, quantity, amount, status, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, order.getOrderNo(), order.getUserId(), order.getProductName(),
                    order.getQuantity(), order.getAmount(),
                    order.getStatus() != null ? order.getStatus() : "PENDING",
                    LocalDateTime.now(), LocalDateTime.now());
        } else {
            jdbcTemplate.update("""
                UPDATE orders SET order_no=?, user_id=?, product_name=?, quantity=?,
                amount=?, status=?, update_time=? WHERE id=?
            """, order.getOrderNo(), order.getUserId(), order.getProductName(),
                    order.getQuantity(), order.getAmount(), order.getStatus(),
                    LocalDateTime.now(), order.getId());
        }
        return findByOrderNo(order.getOrderNo());
    }

    public Order findById(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM orders WHERE id = ?",
                ROW_MAPPER, id);
    }

    public Order findByOrderNo(String orderNo) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM orders WHERE order_no = ?",
                ROW_MAPPER, orderNo);
    }

    public List<Order> findAll() {
        return jdbcTemplate.query("SELECT * FROM orders", ROW_MAPPER);
    }

    public List<Order> findByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM orders WHERE user_id = ?",
                ROW_MAPPER, userId);
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM orders");
    }
}
