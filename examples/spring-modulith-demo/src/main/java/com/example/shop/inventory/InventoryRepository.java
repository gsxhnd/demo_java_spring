package com.example.shop.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 库存仓储
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {
}
