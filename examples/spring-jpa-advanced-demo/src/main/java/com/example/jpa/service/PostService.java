package com.example.jpa.service;

import com.example.jpa.dto.CreatePostRequest;
import com.example.jpa.dto.PostListItemDTO;
import com.example.jpa.dto.PostSearchCriteria;
import com.example.jpa.dto.PostSummaryProjection;
import com.example.jpa.entity.Category;
import com.example.jpa.entity.Post;
import com.example.jpa.repository.CategoryRepository;
import com.example.jpa.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 文章服务
 * 演示 Specification 动态查询、JPA 审计等功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 创建文章（演示 @CreatedBy, @CreatedDate 自动填充）
     */
    @Transactional
    public Post createPost(CreatePostRequest request) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(request.getAuthor())
                .tags(request.getTags())
                .status(Post.PostStatus.DRAFT)
                .viewCount(0)
                .build();

        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .ifPresent(post::setCategory);
        }

        Post saved = postRepository.save(post);
        log.info("创建文章: id={}, title={}, createdBy={}", saved.getId(), saved.getTitle(), saved.getCreatedBy());
        return saved;
    }

    /**
     * 更新文章（演示 @LastModifiedBy, @LastModifiedDate 自动填充）
     */
    @Transactional
    public Post updatePost(Long id, CreatePostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在: " + id));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(request.getAuthor());
        post.setTags(request.getTags());

        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .ifPresent(post::setCategory);
        }

        Post updated = postRepository.save(post);
        log.info("更新文章: id={}, lastModifiedBy={}", updated.getId(), updated.getLastModifiedBy());
        return updated;
    }

    /**
     * 根据 ID 获取文章
     */
    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    /**
     * 获取所有文章（分页）
     */
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    /**
     * 使用接口投影查询文章列表
     */
    public List<PostSummaryProjection> getPostSummaries(Post.PostStatus status) {
        return postRepository.findByStatus(status);
    }

    /**
     * 使用 DTO 投影查询文章列表
     */
    public List<PostListItemDTO> getPostListByAuthor(String author) {
        return postRepository.findByAuthorAsDTO(author);
    }

    /**
     * 使用 Specification 动态查询（核心功能演示）
     */
    public Page<Post> searchPosts(PostSearchCriteria criteria, Pageable pageable) {
        Specification<Post> spec = buildSpecification(criteria);
        return postRepository.findAll(spec, pageable);
    }

    /**
     * 构建动态查询条件
     */
    private Specification<Post> buildSpecification(PostSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 标题模糊匹配
            if (criteria.getTitle() != null && !criteria.getTitle().isBlank()) {
                predicates.add(cb.like(root.get("title"),
                        "%" + criteria.getTitle() + "%"));
            }

            // 作者精确匹配
            if (criteria.getAuthor() != null && !criteria.getAuthor().isBlank()) {
                predicates.add(cb.equal(root.get("author"), criteria.getAuthor()));
            }

            // 状态精确匹配
            if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }

            // 标签模糊匹配
            if (criteria.getTag() != null && !criteria.getTag().isBlank()) {
                predicates.add(cb.like(root.get("tags"),
                        "%" + criteria.getTag() + "%"));
            }

            // 浏览量范围查询
            if (criteria.getMinViewCount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("viewCount"),
                        criteria.getMinViewCount()));
            }
            if (criteria.getMaxViewCount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("viewCount"),
                        criteria.getMaxViewCount()));
            }

            // 分类名称关联查询
            if (criteria.getCategoryName() != null && !criteria.getCategoryName().isBlank()) {
                Join<Post, Category> categoryJoin = root.join("category", JoinType.LEFT);
                predicates.add(cb.equal(categoryJoin.get("name"), criteria.getCategoryName()));
            }

            // 只查询未删除的记录（如果有删除标记）
            // predicates.add(cb.equal(root.get("deleted"), false));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 增加浏览次数
     */
    @Transactional
    public void incrementViewCount(Long id) {
        postRepository.findById(id).ifPresent(Post::incrementViewCount);
    }

    /**
     * 发布文章
     */
    @Transactional
    public Post publishPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在: " + id));
        post.setStatus(Post.PostStatus.PUBLISHED);
        return postRepository.save(post);
    }
}
