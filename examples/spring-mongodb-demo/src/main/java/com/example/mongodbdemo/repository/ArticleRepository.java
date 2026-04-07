package com.example.mongodbdemo.repository;

import com.example.mongodbdemo.entity.Article;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ArticleRepository extends MongoRepository<Article, String> {

    // Derived query: find by author
    List<Article> findByAuthor(String author);

    // Derived query: find articles containing a specific tag
    List<Article> findByTagsContaining(String tag);

    // Custom MongoDB JSON query: search title or content by keyword (regex)
    @Query("{ '$or': [ { 'title': { '$regex': ?0, '$options': 'i' } }, { 'content': { '$regex': ?0, '$options': 'i' } } ] }")
    List<Article> searchByKeyword(String keyword);
}
