package com.example.batch.repository;

import com.example.batch.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Product Repository
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByProcessedTrue();

    List<Product> findByProcessedFalse();
}
