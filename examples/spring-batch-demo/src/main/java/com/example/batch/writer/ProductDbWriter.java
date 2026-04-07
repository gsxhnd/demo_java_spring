package com.example.batch.writer;

import com.example.batch.domain.ProcessedProduct;
import com.example.batch.domain.Product;
import com.example.batch.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * 数据库写入器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductDbWriter implements ItemWriter<ProcessedProduct> {

    private final ProductRepository productRepository;

    @Override
    public void write(Chunk<? extends ProcessedProduct> chunk) throws Exception {
        log.info("批量写入 {} 条记录到数据库", chunk.size());

        for (ProcessedProduct processed : chunk) {
            // 查找或创建产品
            Product product = productRepository.findById(processed.getId())
                    .orElse(Product.builder()
                            .id(processed.getId())
                            .name(processed.getName())
                            .category(processed.getCategory())
                            .price(java.math.BigDecimal.valueOf(processed.getPrice()))
                            .stock(processed.getStock())
                            .processed(false)
                            .build());

            // 更新产品信息
            product.setName(processed.getName());
            product.setCategory(processed.getCategory());
            product.setPrice(java.math.BigDecimal.valueOf(processed.getPrice()));
            product.setStock(processed.getStock());
            product.setProcessed(true);

            productRepository.save(product);
        }

        log.info("数据库写入完成");
    }
}
