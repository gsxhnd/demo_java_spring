package com.example.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private Long id;
    private String accountNo;
    private String holderName;
    private BigDecimal balance;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
