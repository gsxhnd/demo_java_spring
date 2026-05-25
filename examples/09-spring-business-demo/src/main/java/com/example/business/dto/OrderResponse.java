package com.example.business.dto;

import com.example.business.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "订单响应")
public class OrderResponse {

    private Long id;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String customerEmail;
    private String status;
    private LocalDateTime createdAt;
    private String asyncNotificationStatus;

    public static OrderResponse fromEntity(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .customerEmail(order.getCustomerEmail())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
