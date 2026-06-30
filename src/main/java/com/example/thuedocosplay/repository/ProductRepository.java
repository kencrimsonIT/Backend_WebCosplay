package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.Category;
import com.example.thuedocosplay.entity.Product;
import com.example.thuedocosplay.entity.enums.ProductInventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    long countByCategory(Category category);

    List<Product> findAllBySeller_IdOrderByIdDesc(Long sellerId);

    Optional<Product> findByIdAndSeller_Id(Long id, Long sellerId);

    long countBySeller_Id(Long sellerId);

    long countBySeller_IdAndVisible(Long sellerId, Boolean visible);

    long countBySeller_IdAndInventoryStatus(Long sellerId, ProductInventoryStatus status);

    @Query("""
            SELECT COUNT(oi)
            FROM OrderItem oi
            WHERE oi.product.id = :productId
            """)
    long countOrderItemsByProductId(@Param("productId") Long productId);
}
