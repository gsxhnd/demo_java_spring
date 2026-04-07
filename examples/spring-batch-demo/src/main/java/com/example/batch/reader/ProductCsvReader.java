package com.example.batch.reader;

import com.example.batch.domain.ProductCsv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * CSV 文件读取器
 */
@Component
@Slf4j
public class ProductCsvReader {

    /**
     * 创建产品 CSV 读取器
     * CSV 格式: id,name,category,price,stock
     */
    public FlatFileItemReader<ProductCsv> createCsvReader() {
        BeanWrapperFieldSetMapper<ProductCsv> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(ProductCsv.class);

        return new FlatFileItemReaderBuilder<ProductCsv>()
                .name("productCsvReader")
                .resource(new ClassPathResource("data/in/products.csv"))
                .delimited()
                .names("id", "name", "category", "price", "stock")
                .fieldSetMapper(fieldSetMapper)
                .linesToSkip(1)  // 跳过表头
                .build();
    }

    /**
     * 创建定长文件读取器
     * 格式: ID(5) | NAME(20) | CATEGORY(15) | PRICE(10) | STOCK(5)
     */
    public FlatFileItemReader<ProductCsv> createFixedLengthReader() {
        BeanWrapperFieldSetMapper<ProductCsv> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(ProductCsv.class);

        return new FlatFileItemReaderBuilder<ProductCsv>()
                .name("productFixedLengthReader")
                .resource(new ClassPathResource("data/in/products_fixed.txt"))
                .fixedLength()
                .columns(new Range(1, 5), new Range(6, 25), new Range(26, 40), new Range(41, 50), new Range(51, 55))
                .names("id", "name", "category", "price", "stock")
                .fieldSetMapper(fieldSetMapper)
                .build();
    }
}
