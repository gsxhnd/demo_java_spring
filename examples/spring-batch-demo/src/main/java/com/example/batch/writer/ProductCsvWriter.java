package com.example.batch.writer;

import com.example.batch.domain.ProcessedProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * CSV 文件写入器
 */
@Component
@Slf4j
public class ProductCsvWriter {

    private static final String OUTPUT_DIR = "target/batch-output/";

    /**
     * 创建 CSV 写入器
     */
    public FlatFileItemWriter<ProcessedProduct> createCsvWriter() {
        // 确保输出目录存在
        new File(OUTPUT_DIR).mkdirs();

        BeanWrapperFieldExtractor<ProcessedProduct> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"id", "name", "category", "price", "stock", "discountPrice", "status"});

        FormatterLineAggregator<ProcessedProduct> lineAggregator = new FormatterLineAggregator<>();
        lineAggregator.setFieldExtractor(fieldExtractor);
        lineAggregator.setFormat("%-5d|%-20s|%-15s|%10.2f|%5d|%10.2f|%10s");

        return new FlatFileItemWriterBuilder<ProcessedProduct>()
                .name("productCsvWriter")
                .resource(new FileSystemResource(OUTPUT_DIR + "processed_products.csv"))
                .headerCallback(writer -> writer.write("ID|NAME|CATEGORY|PRICE|STOCK|DISCOUNT_PRICE|STATUS"))
                .lineAggregator(lineAggregator)
                .build();
    }

    /**
     * 创建 JSON 写入器（简化版）
     */
    public FlatFileItemWriter<ProcessedProduct> createJsonWriter() {
        return new FlatFileItemWriterBuilder<ProcessedProduct>()
                .name("productJsonWriter")
                .resource(new FileSystemResource(OUTPUT_DIR + "processed_products.json"))
                .lineAggregator(item -> {
                    return String.format(
                            "{\"id\":%d,\"name\":\"%s\",\"category\":\"%s\",\"price\":%.2f,\"stock\":%d,\"discountPrice\":%.2f,\"status\":\"%s\"}",
                            item.getId(), item.getName(), item.getCategory(),
                            item.getPrice(), item.getStock(), item.getDiscountPrice(), item.getStatus()
                    );
                })
                .build();
    }
}
