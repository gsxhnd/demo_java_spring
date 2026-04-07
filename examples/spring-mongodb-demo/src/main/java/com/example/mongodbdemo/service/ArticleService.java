package com.example.mongodbdemo.service;

import com.example.mongodbdemo.entity.Article;
import com.example.mongodbdemo.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final MongoTemplate mongoTemplate;

    // --- MongoRepository-based operations ---

    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    public Optional<Article> findById(String id) {
        return articleRepository.findById(id);
    }

    public Article save(Article article) {
        return articleRepository.save(article);
    }

    public void deleteById(String id) {
        articleRepository.deleteById(id);
    }

    public List<Article> searchByKeyword(String keyword) {
        return articleRepository.searchByKeyword(keyword);
    }

    // --- MongoTemplate-based operations ---

    // Complex criteria query: find articles by author with viewCount >= minViews
    public List<Article> findByAuthorWithMinViews(String author, int minViews) {
        Query query = new Query(
                Criteria.where("author").is(author)
                        .and("viewCount").gte(minViews)
        );
        return mongoTemplate.find(query, Article.class);
    }

    // Update specific fields without replacing the entire document
    public void incrementViewCount(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().inc("viewCount", 1);
        mongoTemplate.updateFirst(query, update, Article.class);
    }

    // Aggregation pipeline: group by author, count articles per author
    public List<Map> getArticleCountByAuthor() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("author").count().as("articleCount"),
                Aggregation.project("articleCount").and("_id").as("author"),
                Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "articleCount")
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "articles", Map.class);
        return results.getMappedResults();
    }
}
