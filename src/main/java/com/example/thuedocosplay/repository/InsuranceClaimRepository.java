package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.InsuranceClaim;
import com.example.thuedocosplay.entity.InsuranceClaim.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> {

    Optional<InsuranceClaim> findByClaimCode(String claimCode);

    Page<InsuranceClaim> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<InsuranceClaim> findByStatusOrderByCreatedAtDesc(ClaimStatus status, Pageable pageable);

    long countByStatus(ClaimStatus status);

    @Query("SELECT COALESCE(SUM(c.approvedAmount), 0) FROM InsuranceClaim c " +
            "WHERE c.status = 'APPROVED' AND c.resolvedAt >= :from AND c.resolvedAt < :to")
    BigDecimal sumApprovedPayout(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
