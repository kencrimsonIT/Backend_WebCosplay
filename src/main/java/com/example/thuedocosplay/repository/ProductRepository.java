package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.Category;
import com.example.thuedocosplay.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    long countByCategory(Category category);
}
