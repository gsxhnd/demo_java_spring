package com.example.jpa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关闭接口投影 - 只查询需要的字段
 * 适合只读场景，减少数据传输
 */
public interface PostSummaryProjection {

    Long getId();

    String getTitle();

    String getAuthor();

    String getStatus();

    Integer getViewCount();
}
