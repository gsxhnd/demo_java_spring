package com.example.transaction.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 创建订单请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于 0.01")
    private BigDecimal amount;

    @NotBlank(message = "支付账户不能为空")
    private String fromAccountNo;
}
