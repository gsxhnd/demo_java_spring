package com.example.esdemo.controller;

import com.example.esdemo.entity.ProductDoc;
import com.example.esdemo.repository.ProductDocRepository;
import com.example.esdemo.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductDocRepository productDocRepository;
    private final ProductSearchService productSearchService;

    // Index a product document
    @PostMapping
    public ProductDoc index(@RequestBody ProductDoc product) {
        return productDocRepository.save(product);
    }

    // Search by keyword with highlight
    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String keyword) {
        return productSearchService.highlightSearch(keyword);
    }

    // Aggregation stats: brand counts + avg price
    @GetMapping("/aggs")
    public Map<String, Object> aggs() {
        return productSearchService.aggregationStats();
    }

    // Delete a product by id
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        productDocRepository.deleteById(id);
    }
}
