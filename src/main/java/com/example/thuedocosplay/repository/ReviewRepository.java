package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Tìm đánh giá của 1 user cho 1 sản phẩm (kiểm tra đã review chưa)
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    // Kiểm tra user đã đánh giá sản phẩm này chưa
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // Lấy tất cả đánh giá của 1 sản phẩm
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    // Tính rating trung bình của sản phẩm
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.product.id = :productId")
    Double avgRatingByProductId(@Param("productId") Long productId);

    // Đếm số đánh giá của sản phẩm
    long countByProductId(Long productId);
}
