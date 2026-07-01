package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.Category;
import com.example.thuedocosplay.entity.Product;
import com.example.thuedocosplay.entity.enums.ProductInventoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository 
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // ─── Thống kê danh mục & gian hàng ───────────────────────────────────────
    long countByCategory(Category category);
    
    long countBySeller_Id(Long sellerId);

    long countBySeller_IdAndVisible(Long sellerId, Boolean visible);

    long countBySeller_IdAndInventoryStatus(Long sellerId, ProductInventoryStatus status);

    // ─── Truy vấn sản phẩm theo gian hàng ────────────────────────────────────
    List<Product> findAllBySeller_IdOrderByIdDesc(Long sellerId);

    Optional<Product> findByIdAndSeller_Id(Long id, Long sellerId);

    // ─── Trang chủ: sản phẩm mới nhất ────────────────────────────────────────
    @Query("SELECT p FROM Product p WHERE p.visible = true ORDER BY p.id DESC")
    List<Product> findTopNewest(Pageable pageable);

    // ─── Trang chủ: bán chạy nhất (Đơn giản hóa cho hiệu năng) ─────────────────
    @Query("""
            SELECT p FROM Product p
            JOIN OrderItem oi ON oi.product.id = p.id
            WHERE p.visible = true
            GROUP BY p.id
            ORDER BY SUM(oi.quantity) DESC
            """)
    List<Product> findTopBestSellerSimple(Pageable pageable);

    // ─── Thống kê đơn hàng liên quan đến sản phẩm ────────────────────────────
    @Query("""
            SELECT COUNT(oi)
            FROM OrderItem oi
            WHERE oi.product.id = :productId
            """)
    long countOrderItemsByProductId(@Param("productId") Long productId);
}