package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Voucher> findAllBySeller_IdOrderByCreatedAtDesc(Long sellerId);
}
