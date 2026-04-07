package com.example.jpa.config;

import com.example.jpa.entity.Category;
import com.example.jpa.entity.Post;
import com.example.jpa.repository.CategoryRepository;
import com.example.jpa.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * 数据初始化配置
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(PostRepository postRepository,
                                        CategoryRepository categoryRepository) {
        return args -> {
            // 获取分类
            Category techCategory = categoryRepository.findByName("Technology")
                    .orElseGet(() -> {
                        Category c = Category.builder()
                                .name("Technology")
                                .description("技术相关文章")
                                .build();
                        return categoryRepository.save(c);
                    });

            Category springCategory = categoryRepository.findByName("Spring")
                    .orElseGet(() -> {
                        Category c = Category.builder()
                                .name("Spring")
                                .description("Spring 框架")
                                .parent(techCategory)
                                .build();
                        return categoryRepository.save(c);
                    });

            // 创建示例文章
            if (postRepository.count() == 0) {
                Post post1 = Post.builder()
                        .title("Spring Boot 入门指南")
                        .content("这是一篇关于 Spring Boot 入门的基础教程...")
                        .author("zhangsan")
                        .tags("spring,java,boot")
                        .status(Post.PostStatus.PUBLISHED)
                        .viewCount(100)
                        .category(springCategory)
                        .build();
                postRepository.save(post1);

                Post post2 = Post.builder()
                        .title("JPA 进阶特性详解")
                        .content("本文详细介绍 JPA 的 Specification、审计、投影等进阶特性...")
                        .author("lisi")
                        .tags("jpa,java,orm")
                        .status(Post.PostStatus.PUBLISHED)
                        .viewCount(250)
                        .category(springCategory)
                        .build();
                postRepository.save(post2);

                Post post3 = Post.builder()
                        .title("分布式系统设计原则")
                        .content("本文讨论分布式系统设计的核心原则和最佳实践...")
                        .author("wangwu")
                        .tags("distributed,architecture")
                        .status(Post.PostStatus.DRAFT)
                        .viewCount(0)
                        .category(techCategory)
                        .build();
                postRepository.save(post3);

                log.info("创建示例文章: {} 篇", postRepository.count());
            }

            log.info("JPA 进阶特性演示数据初始化完成");
            log.info("分类数量: {}", categoryRepository.count());
            log.info("文章数量: {}", postRepository.count());
        };
    }
}
