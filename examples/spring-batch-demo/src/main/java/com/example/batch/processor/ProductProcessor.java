package com.example.batch.processor;

import com.example.batch.domain.ProductCsv;
import com.example.batch.domain.ProcessedProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 数据处理器
 * 负责数据转换和过滤
 */
@Component
@Slf4j
public class ProductProcessor implements ItemProcessor<ProductCsv, ProcessedProduct> {

    @Override
    public ProcessedProduct process(ProductCsv item) throws Exception {
        log.debug("处理产品: id={}, name={}", item.getId(), item.getName());

        // 过滤掉价格为 0 的产品
        if (item.getPrice() == null || item.getPrice().isBlank()) {
            log.debug("跳过无效价格: id={}", item.getId());
            return null;
        }

        try {
            BigDecimal price = new BigDecimal(item.getPrice().trim());
            Integer stock = item.getStock() != null ? Integer.parseInt(item.getStock().trim()) : 0;

            // 计算折扣价（库存 > 100 打 9 折）
            double discountPrice = price.doubleValue();
            String status = "NORMAL";
            if (stock > 100) {
                discountPrice = price.doubleValue() * 0.9;
                status = "DISCOUNTED";
            }

            return ProcessedProduct.builder()
                    .id(Long.parseLong(item.getId().trim()))
                    .name(item.getName().trim())
                    .category(item.getCategory() != null ? item.getCategory().trim() : "UNKNOWN")
                    .price(price.doubleValue())
                    .stock(stock)
                    .discountPrice(Math.round(discountPrice * 100.0) / 100.0)
                    .status(status)
                    .build();

        } catch (NumberFormatException e) {
            log.warn("数据格式错误，跳过: id={}, price={}", item.getId(), item.getPrice());
            return null;
        }
    }
}
