package com.example.mongodbdemo.controller;

import com.example.mongodbdemo.entity.Article;
import com.example.mongodbdemo.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    // GET /api/articles - list all articles
    @GetMapping
    public List<Article> list() {
        return articleService.findAll();
    }

    // GET /api/articles/{id} - get article by id
    @GetMapping("/{id}")
    public ResponseEntity<Article> getById(@PathVariable String id) {
        // Increment view count on each read
        articleService.incrementViewCount(id);
        return articleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/articles - create article
    @PostMapping
    public Article create(@RequestBody Article article) {
        return articleService.save(article);
    }

    // PUT /api/articles/{id} - update article
    @PutMapping("/{id}")
    public ResponseEntity<Article> update(@PathVariable String id, @RequestBody Article article) {
        return articleService.findById(id)
                .map(existing -> {
                    article.setId(id);
                    return ResponseEntity.ok(articleService.save(article));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/articles/{id} - delete article
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return articleService.findById(id)
                .map(existing -> {
                    articleService.deleteById(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/articles/search?keyword=xxx - search by keyword in title/content
    @GetMapping("/search")
    public List<Article> search(@RequestParam String keyword) {
        return articleService.searchByKeyword(keyword);
    }

    // GET /api/articles/stats - aggregation: article count grouped by author
    @GetMapping("/stats")
    public List<Map> stats() {
        return articleService.getArticleCountByAuthor();
    }
}
