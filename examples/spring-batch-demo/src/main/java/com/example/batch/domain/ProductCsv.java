package com.example.batch.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CSV 行数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCsv {

    private Long id;
    private String name;
    private String category;
    private String price;
    private String stock;
}
