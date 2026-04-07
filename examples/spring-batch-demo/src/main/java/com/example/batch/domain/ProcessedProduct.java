package com.example.batch.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 处理后的产品数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedProduct {

    private Long id;
    private String name;
    private String category;
    private Double price;
    private Integer stock;
    private Double discountPrice;
    private String status;
}
