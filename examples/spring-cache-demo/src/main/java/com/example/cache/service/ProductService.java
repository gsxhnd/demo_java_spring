package com.example.cache.service;

import com.example.cache.entity.Product;
import com.example.cache.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 产品服务 - 演示 Spring Cache 注解
 *
 * @Cacheable: 查缓存，miss 则执行方法并缓存结果
 * @CachePut: 执行方法并更新缓存
 * @CacheEvict: 删除缓存
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 查询单个产品 - 使用 @Cacheable
     *
     * cacheNames: 缓存名称
     * key: 缓存键（SpEL 表达式）
     * unless: 条件不成立时跳过缓存（例如 null 值）
     */
    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public Optional<Product> getProduct(Long id) {
        log.info("从数据库查询产品: id={}", id);
        return productRepository.findById(id);
    }

    /**
     * 查询所有产品 - 不缓存（数据量大）
     */
    public List<Product> getAllProducts() {
        log.info("查询所有产品（未缓存）");
        return productRepository.findAll();
    }

    /**
     * 根据分类查询 - 使用 SpEL 表达式作为 key
     */
    @Cacheable(value = "products", key = "'category:' + #category", unless = "#result.isEmpty()")
    public List<Product> getProductsByCategory(String category) {
        log.info("从数据库查询分类产品: category={}", category);
        return productRepository.findByCategory(category);
    }

    /**
     * 搜索产品 - 不缓存（临时查询）
     */
    public List<Product> searchProducts(String keyword) {
        log.info("搜索产品（未缓存）: keyword={}", keyword);
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * 创建产品 - 使用 @CachePut 更新缓存
     */
    @CachePut(value = "products", key = "#result.id")
    @Transactional
    public Product createProduct(Product product) {
        log.info("创建产品: {}", product.getName());
        return productRepository.save(product);
    }

    /**
     * 更新产品 - 使用 @CacheEvict + @Cacheable 组合
     * 先删除旧缓存，再查询新数据并缓存
     */
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products", key = "'category:' + #category")
    })
    @Transactional
    public Product updateProduct(Long id, String name, BigDecimal price, String category) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("产品不存在: " + id));

        String oldCategory = product.getCategory();

        product.setName(name);
        product.setPrice(price);
        product.setCategory(category);

        Product saved = productRepository.save(product);

        // 如果分类变更，也清除新分类的缓存
        if (!oldCategory.equals(category)) {
            log.debug("产品分类变更，清除新分类缓存: {}", category);
        }

        return saved;
    }

    /**
     * 删除产品 - 使用 @CacheEvict
     */
    @CacheEvict(value = "products", key = "#id")
    @Transactional
    public void deleteProduct(Long id) {
        log.info("删除产品: id={}", id);
        productRepository.deleteById(id);
    }

    /**
     * 清空所有产品缓存 - 使用 allEntries = true
     */
    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void clearAllProductsCache() {
        log.info("清空所有产品缓存");
    }

    /**
     * 更新库存（演示缓存同步）
     */
    @CacheEvict(value = "products", key = "#id")
    @Transactional
    public Product updateStock(Long id, Integer stock) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("产品不存在: " + id));

        product.setStock(stock);
        return productRepository.save(product);
    }
}
