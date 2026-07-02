package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Query("""
            SELECT v FROM Voucher v
            WHERE v.seller.id = :sellerId
              AND (v.deleted IS NULL OR v.deleted = false)
            ORDER BY v.createdAt DESC
            """)
    List<Voucher> findVisibleBySellerId(@Param("sellerId") Long sellerId);

    @Query("""
            SELECT COUNT(v) FROM Voucher v
            WHERE v.seller.id = :sellerId
              AND v.status = 'ACTIVE'
              AND (v.deleted IS NULL OR v.deleted = false)
            """)
    long countActiveBySeller(@Param("sellerId") Long sellerId);

    @Query("""
            SELECT COALESCE(SUM(v.usedCount), 0) FROM Voucher v
            WHERE v.seller.id = :sellerId
              AND (v.deleted IS NULL OR v.deleted = false)
            """)
    Long sumUsedCountBySeller(@Param("sellerId") Long sellerId);

    @Query("""
            SELECT COUNT(v) FROM Voucher v
            WHERE v.seller.id = :sellerId
              AND v.status = 'ACTIVE'
              AND v.endsAt >= :now
              AND v.endsAt <= :until
              AND (v.deleted IS NULL OR v.deleted = false)
            """)
    long countExpiringSoonBySeller(
            @Param("sellerId") Long sellerId,
            @Param("now") LocalDateTime now,
            @Param("until") LocalDateTime until
    );
}
