package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.Category;
import com.example.thuedocosplay.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    long countByCategory(Category category);

    // ─── Trang chủ: sản phẩm mới nhất ──────────────────────────────────────
    @Query("SELECT p FROM Product p WHERE p.visible = true ORDER BY p.id DESC")
    List<Product> findTopNewest(Pageable pageable);

    // ─── Trang chủ: bán chạy nhất (theo số lượt thuê trong OrderItem) ──────
    @Query("""
            SELECT p FROM Product p
            WHERE p.visible = true
              AND p.id IN (
                SELECT oi.product.id FROM OrderItem oi
                GROUP BY oi.product.id
                ORDER BY SUM(oi.quantity) DESC
              )
            """)
    List<Product> findTopBestSeller(Pageable pageable);

    // Fallback: bán chạy nhất đơn giản nếu query trên không hoạt động với một số DB
    @Query("""
            SELECT p FROM Product p
            JOIN OrderItem oi ON oi.product.id = p.id
            WHERE p.visible = true
            GROUP BY p.id
            ORDER BY SUM(oi.quantity) DESC
            """)
    List<Product> findTopBestSellerSimple(Pageable pageable);
}
