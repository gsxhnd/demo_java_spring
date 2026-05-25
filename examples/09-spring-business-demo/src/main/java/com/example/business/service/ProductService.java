package com.example.business.service;

import com.example.business.dto.CreateProductRequest;
import com.example.business.dto.ProductResponse;
import com.example.business.entity.Product;
import com.example.business.exception.ResourceNotFoundException;
import com.example.business.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    public static final String CACHE_NAME = "products";

    private final ProductRepository productRepository;

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Cacheable(value = CACHE_NAME, key = "#id", unless = "#result == null")
    public ProductResponse findById(Long id) {
        log.info("从数据库加载商品（未命中缓存时执行）- id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在 - id: " + id));
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .createdAt(now)
                .updatedAt(now)
                .build();
        Product saved = productRepository.save(product);
        log.info("商品创建成功 - id: {}", saved.getId());
        return ProductResponse.fromEntity(saved);
    }

    @CachePut(value = CACHE_NAME, key = "#id")
    @Transactional
    public ProductResponse update(Long id, CreateProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在 - id: " + id));
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setStock(request.getStock());
        existing.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(existing);
        log.info("商品更新并刷新缓存 - id: {}", id);
        return ProductResponse.fromEntity(saved);
    }

    @CacheEvict(value = CACHE_NAME, key = "#id")
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("商品不存在 - id: " + id);
        }
        productRepository.deleteById(id);
        log.info("商品删除并清除缓存 - id: {}", id);
    }

    @CacheEvict(value = CACHE_NAME, key = "#id")
    @Transactional
    public void evictCache(Long id) {
        log.info("手动清除商品缓存 - id: {}", id);
    }

    @Transactional
    public Product getProductEntityForUpdate(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在 - id: " + id));
    }

    @Transactional
    public void saveProductEntity(Product product) {
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }
}
