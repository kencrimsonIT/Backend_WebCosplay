package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.Review;
import com.example.thuedocosplay.entity.enums.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Danh sách review cho admin kiểm duyệt, lọc theo trạng thái
    Page<Review> findByModerationStatusOrderByCreatedAtDesc(
            ModerationStatus status, Pageable pageable);

    // Tất cả review (mọi trạng thái) cho admin xem toàn bộ
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Chỉ lấy review đã duyệt khi hiển thị public (dùng ở ProductDetail)
    List<Review> findByProductIdAndModerationStatusOrderByCreatedAtDesc(
            Long productId, ModerationStatus status);

    // Đếm theo từng trạng thái (cho dashboard số liệu)
    long countByModerationStatus(ModerationStatus status);

    // Review bị báo cáo nhiều — ưu tiên xử lý
    @org.springframework.data.jpa.repository.Query(
            "SELECT r FROM Review r WHERE r.reportCount > 0 ORDER BY r.reportCount DESC, r.createdAt DESC")
    List<Review> findMostReported(Pageable pageable);

}
