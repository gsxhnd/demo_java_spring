package com.example.mysqldemo.repository;

import com.example.mysqldemo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Product Repository - PostgreSQL JSONB 示例
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 使用原生 SQL 查询 JSONB 字段
     * PostgreSQL JSONB 查询示例
     */
    @Query(value = """
            SELECT * FROM products
            WHERE attributes->>'category' = :category
            """, nativeQuery = true)
    List<Product> findByJsonCategory(@Param("category") String category);

    /**
     * 查询包含特定标签的产品
     */
    @Query(value = """
            SELECT * FROM products
            WHERE :tag = ANY(tags)
            """, nativeQuery = true)
    List<Product> findByTag(@Param("tag") String tag);

    /**
     * JSONB 包含查询
     */
    @Query(value = """
            SELECT * FROM products
            WHERE attributes @> :jsonFilter
            """, nativeQuery = true)
    List<Product> findByAttributesContaining(@Param("jsonFilter") String jsonFilter);
}
