package com.example.jpa.controller;

import com.example.jpa.dto.CreatePostRequest;
import com.example.jpa.dto.PostListItemDTO;
import com.example.jpa.dto.PostSearchCriteria;
import com.example.jpa.dto.PostSummaryProjection;
import com.example.jpa.entity.Post;
import com.example.jpa.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文章控制器
 * 演示 JPA 进阶特性的各种用法
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 创建文章
     */
    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody CreatePostRequest request) {
        Post post = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable Long id) {
        return postService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取所有文章（分页）
     */
    @GetMapping
    public ResponseEntity<Page<Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(postService.getAllPosts(pageRequest));
    }

    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody CreatePostRequest request) {
        Post post = postService.updatePost(id, request);
        return ResponseEntity.ok(post);
    }

    /**
     * 使用接口投影获取文章摘要列表
     */
    @GetMapping("/summaries")
    public ResponseEntity<List<PostSummaryProjection>> getPostSummaries(
            @RequestParam(required = false) String status) {
        Post.PostStatus postStatus = status != null
                ? Post.PostStatus.valueOf(status.toUpperCase())
                : Post.PostStatus.PUBLISHED;
        return ResponseEntity.ok(postService.getPostSummaries(postStatus));
    }

    /**
     * 使用 DTO 投影获取作者的文章列表
     */
    @GetMapping("/by-author/{author}")
    public ResponseEntity<List<PostListItemDTO>> getPostsByAuthor(@PathVariable String author) {
        return ResponseEntity.ok(postService.getPostListByAuthor(author));
    }

    /**
     * 动态查询文章（使用 Specification）
     * 支持任意组合的查询条件
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Post>> searchPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Integer minViewCount,
            @RequestParam(required = false) Integer maxViewCount,
            @RequestParam(required = false) String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PostSearchCriteria criteria = PostSearchCriteria.builder()
                .title(title)
                .author(author)
                .status(status)
                .tag(tag)
                .minViewCount(minViewCount)
                .maxViewCount(maxViewCount)
                .categoryName(categoryName)
                .build();

        PageRequest pageRequest = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.searchPosts(criteria, pageRequest));
    }

    /**
     * 增加浏览次数
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<Map<String, Object>> incrementViewCount(@PathVariable Long id) {
        postService.incrementViewCount(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "浏览次数已增加");
        response.put("postId", id);
        return ResponseEntity.ok(response);
    }

    /**
     * 发布文章
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<Post> publishPost(@PathVariable Long id) {
        Post post = postService.publishPost(id);
        return ResponseEntity.ok(post);
    }
}
