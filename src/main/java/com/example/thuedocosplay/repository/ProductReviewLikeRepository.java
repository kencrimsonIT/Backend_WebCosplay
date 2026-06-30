package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.ProductReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductReviewLikeRepository extends JpaRepository<ProductReviewLike, Long> {
    long countByReview_Id(Long reviewId);

    boolean existsByReview_IdAndUser_Id(Long reviewId, Long userId);

    Optional<ProductReviewLike> findByReview_IdAndUser_Id(Long reviewId, Long userId);
}
