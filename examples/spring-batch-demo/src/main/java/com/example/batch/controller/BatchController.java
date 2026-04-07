package com.example.batch.controller;

import com.example.batch.domain.Product;
import com.example.batch.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Batch 控制器
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job csvImportJob;
    private final ProductRepository productRepository;

    /**
     * 触发 CSV 导入 Job
     */
    @PostMapping("/jobs/csv-import")
    public ResponseEntity<Map<String, Object>> runCsvImportJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("timestamp", LocalDateTime.now().toString())
                    .toJobParameters();

            jobLauncher.run(csvImportJob, params);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "CSV 导入 Job 已启动");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取处理后的产品列表
     */
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false, defaultValue = "true") Boolean processed) {

        List<Product> products = processed
                ? productRepository.findByProcessedTrue()
                : productRepository.findByProcessedFalse();

        return ResponseEntity.ok(products);
    }

    /**
     * 获取所有产品
     */
    @GetMapping("/products/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    /**
     * 获取批处理统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<Product> allProducts = productRepository.findAll();
        long processed = productRepository.findByProcessedTrue().size();
        long unprocessed = productRepository.findByProcessedFalse().size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", allProducts.size());
        stats.put("processedProducts", processed);
        stats.put("unprocessedProducts", unprocessed);
        stats.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(stats);
    }
}
