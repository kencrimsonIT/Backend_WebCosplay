package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByTxnRef(String txnRef);
}
