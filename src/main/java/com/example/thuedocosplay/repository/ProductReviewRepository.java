package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.ProductReview;
import com.example.thuedocosplay.entity.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findAllByProduct_IdAndStatusOrderByCreatedAtDesc(Long productId, ReviewStatus status);

    List<ProductReview> findAllByBuyer_IdOrderByCreatedAtDesc(Long buyerId);

    boolean existsByOrder_IdAndProduct_IdAndBuyer_Id(Long orderId, Long productId, Long buyerId);

    Optional<ProductReview> findByIdAndBuyer_Id(Long id, Long buyerId);

    @Query("""
            SELECT r
            FROM ProductReview r
            JOIN FETCH r.product p
            LEFT JOIN FETCH p.seller s
            JOIN FETCH r.buyer b
            JOIN FETCH r.order o
            WHERE p.seller.id = :sellerId
              AND (:status IS NULL OR r.status = :status)
            ORDER BY r.createdAt DESC
            """)
    List<ProductReview> findSellerReviews(
            @Param("sellerId") Long sellerId,
            @Param("status") ReviewStatus status
    );

    @Query("""
            SELECT r
            FROM ProductReview r
            JOIN FETCH r.product p
            LEFT JOIN FETCH p.seller s
            JOIN FETCH r.buyer b
            JOIN FETCH r.order o
            WHERE r.id = :reviewId
              AND p.seller.id = :sellerId
            """)
    Optional<ProductReview> findSellerReview(
            @Param("reviewId") Long reviewId,
            @Param("sellerId") Long sellerId
    );

    long countByProduct_IdAndStatus(Long productId, ReviewStatus status);

    long countByProduct_Seller_Id(Long sellerId);

    long countByProduct_Seller_IdAndStatus(Long sellerId, ReviewStatus status);

    long countByProduct_Seller_IdAndSellerResponseIsNull(Long sellerId);

    long countByProduct_Seller_IdAndRatingLessThanEqual(Long sellerId, Integer rating);

    @Query("""
            SELECT COALESCE(AVG(r.rating), 0)
            FROM ProductReview r
            WHERE r.product.id = :productId
              AND r.status = :status
            """)
    double averageRatingByProduct(
            @Param("productId") Long productId,
            @Param("status") ReviewStatus status
    );

    @Query("""
            SELECT COALESCE(AVG(r.rating), 0)
            FROM ProductReview r
            WHERE r.product.seller.id = :sellerId
              AND r.status = :status
            """)
    double averageRatingBySeller(
            @Param("sellerId") Long sellerId,
            @Param("status") ReviewStatus status
    );
}
