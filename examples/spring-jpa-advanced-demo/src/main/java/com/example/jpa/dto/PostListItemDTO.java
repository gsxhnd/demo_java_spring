package com.example.jpa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章列表项 DTO - 用于列表展示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListItemDTO {

    private Long id;
    private String title;
    private String author;
    private String status;
    private Integer viewCount;
    private String createdAt;
}
