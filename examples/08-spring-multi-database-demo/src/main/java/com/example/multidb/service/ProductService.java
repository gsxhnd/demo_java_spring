package com.example.multidb.service;

import com.example.multidb.dto.es.CreateProductRequest;
import com.example.multidb.dto.es.ProductResponse;
import com.example.multidb.entity.es.Product;
import com.example.multidb.exception.ResourceNotFoundException;
import com.example.multidb.repository.es.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> findAll() {
        Iterable<Product> products = productRepository.findAll();
        return toList(products).stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    public ProductResponse findById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在 - id: " + id));
        return ProductResponse.fromEntity(product);
    }

    public List<ProductResponse> search(String keyword) {
        return productRepository.findByNameContainingOrDescriptionContaining(keyword, keyword)
                .stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    public ProductResponse create(CreateProductRequest request) {
        Product product = Product.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .build();
        Product saved = productRepository.save(product);
        log.info("Elasticsearch 商品索引 - id: {}, name: {}", saved.getId(), saved.getName());
        return ProductResponse.fromEntity(saved);
    }

    public ProductResponse update(String id, CreateProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在 - id: " + id));
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setCategory(request.getCategory());
        existing.setPrice(request.getPrice());
        Product saved = productRepository.save(existing);
        log.info("Elasticsearch 商品更新 - id: {}", id);
        return ProductResponse.fromEntity(saved);
    }

    public void delete(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("商品不存在 - id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Elasticsearch 商品删除 - id: {}", id);
    }

    private List<Product> toList(Iterable<Product> iterable) {
        List<Product> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }
}
