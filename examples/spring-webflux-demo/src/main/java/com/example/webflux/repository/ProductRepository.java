package com.example.webflux.repository;

import com.example.webflux.entity.Product;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Product Repository (响应式)
 */
@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {

    /**
     * 根据分类查询
     */
    Flux<Product> findByCategory(String category);

    /**
     * 根据名称模糊查询
     */
    Flux<Product> findByNameContaining(String keyword);

    /**
     * 查询有库存的产品
     */
    Flux<Product> findByStockGreaterThan(Integer stock);

    /**
     * 自定义查询
     */
    @Query("SELECT * FROM products WHERE price BETWEEN :minPrice AND :maxPrice")
    Flux<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 统计分类数量
     */
    @Query("SELECT COUNT(*) FROM products WHERE category = :category")
    Mono<Long> countByCategory(String category);
}
