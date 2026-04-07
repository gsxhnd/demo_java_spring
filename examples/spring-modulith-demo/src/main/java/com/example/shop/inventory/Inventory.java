package com.example.shop.inventory;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存实体
 */
@Entity
@Table(name = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    private String productName;

    @Column(nullable = false)
    private Integer availableStock;

    private Integer reservedStock;

    @Column(nullable = false)
    private Integer totalStock;
}
