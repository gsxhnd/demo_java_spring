package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// MongoDB document mapped to "articles" collection
@Data
@Document(collection = "articles")
public class Article {

    @Id
    private String id;

    @Indexed
    private String title;

    private String content;

    private String author;

    private List<String> tags;

    private Map<String, Object> metadata;

    private Integer viewCount;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
