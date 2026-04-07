package com.example.batch.config;

import com.example.batch.domain.ProductCsv;
import com.example.batch.domain.ProcessedProduct;
import com.example.batch.listener.JobCompletionListener;
import com.example.batch.listener.StepProgressListener;
import com.example.batch.processor.ProductProcessor;
import com.example.batch.reader.ProductCsvReader;
import com.example.batch.writer.ProductCsvWriter;
import com.example.batch.writer.ProductDbWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch Job 配置
 */
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ProductCsvReader productCsvReader;
    private final ProductProcessor productProcessor;
    private final ProductDbWriter productDbWriter;
    private final ProductCsvWriter productCsvWriter;
    private final JobCompletionListener jobCompletionListener;
    private final StepProgressListener stepProgressListener;

    /**
     * 定义 Job
     */
    @Bean
    public Job csvImportJob() {
        return new JobBuilder("csvImportJob", jobRepository)
                .listener(jobCompletionListener)
                .start(csvImportStep())
                .build();
    }

    /**
     * 定义 Step（读取-CSV → 处理 → 写入-数据库）
     */
    @Bean
    public Step csvImportStep() {
        return new StepBuilder("csvImportStep", jobRepository)
                .<ProductCsv, ProcessedProduct>chunk(100, transactionManager)  // 每 100 条提交一次事务
                .reader(productCsvReader.createCsvReader())
                .processor(productProcessor)
                .writer(productDbWriter)
                .listener(stepProgressListener)
                .faultTolerant()  // 容错配置
                .skipLimit(10)    // 最多跳过 10 条错误记录
                .skip(Exception.class)
                .retryLimit(3)    // 重试 3 次
                .retry(Exception.class)
                .build();
    }
}
