package com.example.jpa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章查询条件 DTO - 用于 Specification 动态查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchCriteria {

    private String title;

    private String author;

    private String status;

    private String tag;

    private Integer minViewCount;

    private Integer maxViewCount;

    private String categoryName;
}
