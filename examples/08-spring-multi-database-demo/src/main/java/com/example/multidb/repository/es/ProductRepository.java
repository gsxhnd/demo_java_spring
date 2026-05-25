package com.example.multidb.repository.es;

import com.example.multidb.entity.es.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductRepository extends ElasticsearchRepository<Product, String> {

    List<Product> findByNameContainingOrDescriptionContaining(String name, String description);
}
