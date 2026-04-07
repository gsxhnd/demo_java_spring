package com.example.cache.repository;

import com.example.cache.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Product Repository
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrue();

    List<Product> findByCategory(String category);

    @Query("SELECT p FROM Product p WHERE p.stock > 0 AND p.active = true")
    List<Product> findAvailableProducts();

    List<Product> findByNameContainingIgnoreCase(String keyword);
}
