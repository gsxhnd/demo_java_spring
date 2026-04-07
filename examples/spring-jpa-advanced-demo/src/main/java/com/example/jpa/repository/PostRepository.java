package com.example.jpa.repository;

import com.example.jpa.dto.PostSummaryProjection;
import com.example.jpa.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Post Repository
 * 继承 JpaSpecificationExecutor 以支持动态查询
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    /**
     * 使用接口投影查询
     */
    List<PostSummaryProjection> findByStatus(Post.PostStatus status);

    /**
     * 分页查询并投影
     */
    @Query(value = """
            SELECT p.id AS id, p.title AS title, p.author AS author,
                   p.status AS status, p.viewCount AS viewCount
            FROM Post p
            WHERE p.status = :status
            """)
    Page<PostSummaryProjection> findByStatusWithProjection(
            @Param("status") Post.PostStatus status,
            Pageable pageable);

    /**
     * 使用 @Query 进行 DTO 投影
     */
    @Query("""
            SELECT new com.example.jpa.dto.PostListItemDTO(
                p.id, p.title, p.author, p.status.name(),
                p.viewCount, CAST(p.createdAt AS string)
            )
            FROM Post p
            WHERE p.author = :author
            """)
    List<PostListItemDTO> findByAuthorAsDTO(@Param("author") String author);

    /**
     * 带关联查询的投影
     */
    @Query("""
            SELECT p.id AS id, p.title AS title, p.author AS author,
                   p.status AS status, p.viewCount AS viewCount
            FROM Post p LEFT JOIN p.category c
            WHERE c.name = :categoryName
            """)
    List<PostSummaryProjection> findByCategoryName(@Param("categoryName") String categoryName);
}
