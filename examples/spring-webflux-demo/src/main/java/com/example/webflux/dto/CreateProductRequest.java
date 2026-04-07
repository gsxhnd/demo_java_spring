package com.example.webflux.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 创建产品请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "产品名称不能为空")
    private String name;

    private String description;

    @Positive(message = "价格必须大于0")
    private BigDecimal price;

    @Positive(message = "库存必须大于0")
    private Integer stock;

    private String category;
}
