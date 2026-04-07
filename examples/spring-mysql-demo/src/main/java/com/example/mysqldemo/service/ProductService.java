package com.example.mysqldemo.service;

import com.example.mysqldemo.entity.Product;
import com.example.mysqldemo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Product Service - PostgreSQL JSONB 示例
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 创建产品
     */
    @Transactional
    public Product createProduct(String name, String description, Double price,
                                  Integer stock, Map<String, Object> attributes,
                                  List<String> tags) {
        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .attributes(attributes)
                .tags(tags)
                .build();

        Product saved = productRepository.save(product);
        log.info("创建产品: id={}, name={}, attributes={}", saved.getId(), saved.getName(), saved.getAttributes());
        return saved;
    }

    /**
     * 获取所有产品
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * 根据 ID 获取产品
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * 更新产品属性
     */
    @Transactional
    public Product updateProductAttributes(Long id, Map<String, Object> attributes) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("产品不存在: " + id));

        product.setAttributes(attributes);
        return productRepository.save(product);
    }

    /**
     * 添加标签
     */
    @Transactional
    public Product addTag(Long id, String tag) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("产品不存在: " + id));

        if (product.getTags() == null) {
            product.setTags(new java.util.ArrayList<>());
        }
        product.getTags().add(tag);

        return productRepository.save(product);
    }

    /**
     * 根据 JSONB 分类查询
     */
    public List<Product> findByJsonCategory(String category) {
        return productRepository.findByJsonCategory(category);
    }

    /**
     * 根据标签查询
     */
    public List<Product> findByTag(String tag) {
        return productRepository.findByTag(tag);
    }
}
