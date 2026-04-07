package com.example.webflux.service;

import com.example.webflux.dto.CreateProductRequest;
import com.example.webflux.dto.ProductDTO;
import com.example.webflux.entity.Product;
import com.example.webflux.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product Service (响应式)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 获取所有产品
     */
    public Flux<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .map(this::toDTO);
    }

    /**
     * 根据 ID 获取产品
     */
    public Mono<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::toDTO);
    }

    /**
     * 根据分类获取产品
     */
    public Flux<ProductDTO> getProductsByCategory(String category) {
        return productRepository.findByCategory(category)
                .map(this::toDTO);
    }

    /**
     * 搜索产品
     */
    public Flux<ProductDTO> searchProducts(String keyword) {
        return productRepository.findByNameContaining(keyword)
                .map(this::toDTO);
    }

    /**
     * 创建产品
     */
    public Mono<ProductDTO> createProduct(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .createdAt(LocalDateTime.now())
                .build();

        return productRepository.save(product)
                .map(this::toDTO);
    }

    /**
     * 更新产品
     */
    public Mono<ProductDTO> updateProduct(Long id, CreateProductRequest request) {
        return productRepository.findById(id)
                .flatMap(existing -> {
                    existing.setName(request.getName());
                    existing.setDescription(request.getDescription());
                    existing.setPrice(request.getPrice());
                    existing.setStock(request.getStock());
                    existing.setCategory(request.getCategory());
                    return productRepository.save(existing);
                })
                .map(this::toDTO);
    }

    /**
     * 删除产品
     */
    public Mono<Void> deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }

    /**
     * 价格区间查询
     */
    public Flux<ProductDTO> getProductsByPriceRange(BigDecimal min, BigDecimal max) {
        return productRepository.findByPriceRange(min, max)
                .map(this::toDTO);
    }

    /**
     * 聚合查询：获取各类别产品数量
     */
    public Flux<CategoryCount> getCategoryCounts() {
        return getAllProducts()
                .groupBy(ProductDTO::getCategory)
                .flatMap(group -> group.count()
                        .map(count -> new CategoryCount(group.key(), count.intValue())));
    }

    /**
     * 转换实体为 DTO
     */
    private ProductDTO toDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .createdAt(product.getCreatedAt())
                .build();
    }

    /**
     * 类别统计
     */
    public record CategoryCount(String category, int count) {}
}
