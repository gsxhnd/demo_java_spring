package com.example.transaction.repository;

import com.example.transaction.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户仓储层
 */
@Repository
public class AccountRepository {

    private final JdbcTemplate jdbcTemplate;

    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Account> ROW_MAPPER = (rs, rowNum) -> Account.builder()
            .id(rs.getLong("id"))
            .accountNo(rs.getString("account_no"))
            .holderName(rs.getString("holder_name"))
            .balance(rs.getBigDecimal("balance"))
            .createTime(rs.getTimestamp("create_time").toLocalDateTime())
            .updateTime(rs.getTimestamp("update_time").toLocalDateTime())
            .build();

    public void initTable() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS account (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                account_no VARCHAR(50) UNIQUE NOT NULL,
                holder_name VARCHAR(100) NOT NULL,
                balance DECIMAL(19,2) DEFAULT 0.00,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """);
    }

    public Account save(Account account) {
        if (account.getId() == null) {
            jdbcTemplate.update("""
                INSERT INTO account (account_no, holder_name, balance, create_time, update_time)
                VALUES (?, ?, ?, ?, ?)
            """, account.getAccountNo(), account.getHolderName(),
                    account.getBalance(), LocalDateTime.now(), LocalDateTime.now());
        } else {
            jdbcTemplate.update("""
                UPDATE account SET account_no=?, holder_name=?, balance=?, update_time=?
                WHERE id=?
            """, account.getAccountNo(), account.getHolderName(),
                    account.getBalance(), LocalDateTime.now(), account.getId());
        }
        return findByAccountNo(account.getAccountNo());
    }

    public Account findById(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM account WHERE id = ?",
                ROW_MAPPER, id);
    }

    public Account findByAccountNo(String accountNo) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM account WHERE account_no = ?",
                ROW_MAPPER, accountNo);
    }

    public List<Account> findAll() {
        return jdbcTemplate.query("SELECT * FROM account", ROW_MAPPER);
    }

    public void updateBalance(String accountNo, BigDecimal newBalance) {
        jdbcTemplate.update("""
            UPDATE account SET balance = ?, update_time = ?
            WHERE account_no = ?
        """, newBalance, LocalDateTime.now(), accountNo);
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM account");
    }
}
