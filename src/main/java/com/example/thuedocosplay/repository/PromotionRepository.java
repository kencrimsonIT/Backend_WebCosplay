package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.Promotion;
import com.example.thuedocosplay.entity.enums.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findBySeller_IdOrderByCreatedAtDesc(Long sellerId);

    List<Promotion> findBySeller_IdAndStatusOrderByCreatedAtDesc(Long sellerId, PromotionStatus status);

    boolean existsByCodeIgnoreCase(String code);

    Optional<Promotion> findByCodeIgnoreCase(String code);

    @Query("SELECT p FROM Promotion p WHERE p.seller.id = :sellerId AND p.status = 'ACTIVE' " +
            "AND (p.startDate IS NULL OR p.startDate <= :today) " +
            "AND (p.endDate IS NULL OR p.endDate >= :today)")
    List<Promotion> findActiveBySellerAndDate(@Param("sellerId") Long sellerId, @Param("today") LocalDate today);

    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.seller.id = :sellerId AND p.status = 'ACTIVE'")
    long countActiveBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT SUM(p.usedCount) FROM Promotion p WHERE p.seller.id = :sellerId")
    Long sumUsedCountBySeller(@Param("sellerId") Long sellerId);
}
