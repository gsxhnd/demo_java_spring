package com.example.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 博客文章实体 - 演示 JPA 审计功能
 *
 * @CreatedDate / @LastModifiedDate - 自动记录创建/修改时间
 * @CreatedBy / @LastModifiedBy - 自动记录创建/修改人
 */
@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_status", columnList = "status"),
        @Index(name = "idx_posts_author", columnList = "author"),
        @Index(name = "idx_posts_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PostStatus status = PostStatus.DRAFT;

    private String author;

    @Column(length = 500)
    private String tags;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // ========== JPA 审计字段 ==========

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_at", nullable = false)
    private LocalDateTime lastModifiedAt;

    @Version
    private Long version;

    /**
     * 文章状态枚举
     */
    public enum PostStatus {
        DRAFT,      // 草稿
        PUBLISHED,  // 已发布
        ARCHIVED    // 已归档
    }

    /**
     * 增加浏览次数
     */
    public void incrementViewCount() {
        this.viewCount++;
    }
}
