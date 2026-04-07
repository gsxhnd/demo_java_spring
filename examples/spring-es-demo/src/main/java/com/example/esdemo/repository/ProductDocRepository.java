package com.example.esdemo.repository;

import com.example.esdemo.entity.ProductDoc;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductDocRepository extends ElasticsearchRepository<ProductDoc, String> {

    List<ProductDoc> findByBrand(String brand);

    List<ProductDoc> findByPriceBetween(Double minPrice, Double maxPrice);

    @Highlight(fields = {
            @HighlightField(name = "name"),
            @HighlightField(name = "description")
    })
    List<SearchHit<ProductDoc>> findByNameOrDescription(String name, String description);
}
