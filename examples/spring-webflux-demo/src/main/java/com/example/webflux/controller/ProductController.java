package com.example.webflux.controller;

import com.example.webflux.dto.CreateProductRequest;
import com.example.webflux.dto.ProductDTO;
import com.example.webflux.dto.SseEvent;
import com.example.webflux.service.ProductService;
import com.example.webflux.service.SseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Product Controller (响应式)
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final SseService sseService;

    /**
     * 获取所有产品
     */
    @GetMapping
    public Flux<ProductDTO> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * 获取单个产品
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProductDTO>> getProduct(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * 根据分类查询
     */
    @GetMapping("/category/{category}")
    public Flux<ProductDTO> getByCategory(@PathVariable String category) {
        return productService.getProductsByCategory(category);
    }

    /**
     * 搜索产品
     */
    @GetMapping("/search")
    public Flux<ProductDTO> search(@RequestParam String keyword) {
        return productService.searchProducts(keyword);
    }

    /**
     * 价格区间查询
     */
    @GetMapping("/price-range")
    public Flux<ProductDTO> getByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return productService.getProductsByPriceRange(min, max);
    }

    /**
     * 创建产品
     */
    @PostMapping
    public Mono<ResponseEntity<ProductDTO>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        return productService.createProduct(request)
                .map(product -> ResponseEntity.status(HttpStatus.CREATED).body(product));
    }

    /**
     * 更新产品
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductRequest request) {
        return productService.updateProduct(id, request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * 删除产品
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * 获取类别统计
     */
    @GetMapping("/stats/categories")
    public Flux<ProductService.CategoryCount> getCategoryStats() {
        return productService.getCategoryCounts();
    }

    // ========== SSE 端点 ==========

    /**
     * SSE 事件流
     */
    @GetMapping(value = "/sse/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SseEvent> sseStream() {
        return sseService.getEventStream();
    }

    /**
     * 实时时间流
     */
    @GetMapping(value = "/sse/time", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> timeStream() {
        return sseService.getTimeStream();
    }

    /**
     * 触发 SSE 事件
     */
    @PostMapping("/sse/broadcast")
    public ResponseEntity<String> broadcast(@RequestParam String message) {
        sseService.sendEvent("broadcast", message);
        return ResponseEntity.ok("消息已广播");
    }
}
