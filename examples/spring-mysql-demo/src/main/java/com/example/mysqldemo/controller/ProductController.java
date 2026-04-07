package com.example.mysqldemo.controller;

import com.example.mysqldemo.entity.Product;
import com.example.mysqldemo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Product Controller - PostgreSQL JSONB 示例
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 创建产品
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) request.get("attributes");
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) request.get("tags");

        Product product = productService.createProduct(
                (String) request.get("name"),
                (String) request.get("description"),
                Double.parseDouble(request.get("price").toString()),
                Integer.parseInt(request.get("stock").toString()),
                attributes,
                tags
        );

        return ResponseEntity.ok(product);
    }

    /**
     * 获取所有产品
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * 获取单个产品
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新产品属性
     */
    @PutMapping("/{id}/attributes")
    public ResponseEntity<Product> updateAttributes(
            @PathVariable Long id,
            @RequestBody Map<String, Object> attributes) {
        return ResponseEntity.ok(productService.updateProductAttributes(id, attributes));
    }

    /**
     * 添加标签
     */
    @PostMapping("/{id}/tags")
    public ResponseEntity<Product> addTag(
            @PathVariable Long id,
            @RequestParam String tag) {
        return ResponseEntity.ok(productService.addTag(id, tag));
    }

    /**
     * 根据 JSONB 分类查询
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> findByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.findByJsonCategory(category));
    }

    /**
     * 根据标签查询
     */
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<Product>> findByTag(@PathVariable String tag) {
        return ResponseEntity.ok(productService.findByTag(tag));
    }
}
