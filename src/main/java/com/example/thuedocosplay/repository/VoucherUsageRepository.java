package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {
    long countByVoucher_Id(Long voucherId);

    long countByVoucher_IdAndCustomerEmailIgnoreCase(Long voucherId, String customerEmail);

    Optional<VoucherUsage> findByOrder_Id(Long orderId);
}
