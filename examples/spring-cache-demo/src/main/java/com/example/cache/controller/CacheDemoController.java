package com.example.cache.controller;

import com.example.cache.config.MultiLevelCacheService;
import com.example.cache.entity.Product;
import com.example.cache.entity.User;
import com.example.cache.service.ProductService;
import com.example.cache.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓存控制器 - 演示各种缓存操作
 */
@RestController
@RequestMapping("/api/cache-demo")
@RequiredArgsConstructor
public class CacheDemoController {

    private final ProductService productService;
    private final UserService userService;
    private final MultiLevelCacheService multiLevelCacheService;

    // ========== 产品缓存演示 ==========

    /**
     * 获取产品（演示 @Cacheable）
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.getProduct(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取分类产品列表（演示分类缓存）
     */
    @GetMapping("/products/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    /**
     * 搜索产品（不缓存）
     */
    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    /**
     * 创建产品（演示 @CachePut）
     */
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product created = productService.createProduct(product);
        return ResponseEntity.ok(created);
    }

    /**
     * 更新产品（演示 @CacheEvict）
     */
    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) String category) {
        Product updated = productService.updateProduct(id, name, price, category);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除产品
     */
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "产品已删除", "id", id));
    }

    /**
     * 清空所有产品缓存
     */
    @DeleteMapping("/products/cache")
    public ResponseEntity<Map<String, Object>> clearProductsCache() {
        productService.clearAllProductsCache();
        return ResponseEntity.ok(Map.of("message", "产品缓存已清空"));
    }

    // ========== 用户缓存演示 ==========

    /**
     * 获取用户
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.getUser(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据用户名获取用户
     */
    @GetMapping("/users/by-username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== 多级缓存演示 ==========

    /**
     * 多级缓存读取
     */
    @GetMapping("/multi-level/{key}")
    public ResponseEntity<Object> getFromMultiLevelCache(
            @RequestParam String cacheName,
            @PathVariable String key) {
        Object value = multiLevelCacheService.get(cacheName, key);
        if (value != null) {
            return ResponseEntity.ok(value);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 多级缓存写入
     */
    @PostMapping("/multi-level")
    public ResponseEntity<Map<String, Object>> writeMultiLevelCache(
            @RequestParam String cacheName,
            @RequestParam String key,
            @RequestParam Object value,
            @RequestParam(defaultValue = "300") long ttlSeconds) {
        multiLevelCacheService.put(cacheName, key, value, ttlSeconds);
        return ResponseEntity.ok(Map.of(
                "message", "缓存写入成功",
                "cacheName", cacheName,
                "key", key,
                "ttlSeconds", ttlSeconds
        ));
    }

    /**
     * 多级缓存删除
     */
    @DeleteMapping("/multi-level")
    public ResponseEntity<Map<String, Object>> deleteMultiLevelCache(
            @RequestParam String cacheName,
            @RequestParam String key) {
        multiLevelCacheService.evict(cacheName, key);
        return ResponseEntity.ok(Map.of(
                "message", "缓存已删除",
                "cacheName", cacheName,
                "key", key
        ));
    }

    /**
     * 获取缓存统计
     */
    @GetMapping("/stats")
    public ResponseEntity<MultiLevelCacheService.CacheStats> getCacheStats() {
        return ResponseEntity.ok(multiLevelCacheService.getStats());
    }

    /**
     * 测试缓存穿透：查询不存在的数据
     */
    @GetMapping("/penetration-test/{id}")
    public ResponseEntity<Map<String, Object>> penetrationTest(@PathVariable Long id) {
        var product = productService.getProduct(id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("found", product.isPresent());
        return ResponseEntity.ok(result);
    }
}
