package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findAllByCustomer_IdOrderByUpdatedAtDesc(Long customerId);

    Optional<CartItem> findByIdAndCustomer_Id(Long id, Long customerId);

    @Query("""
            SELECT c FROM CartItem c
            WHERE c.customer.id = :customerId
              AND c.product.id = :productId
              AND ((:size IS NULL AND c.size IS NULL) OR c.size = :size)
              AND c.startDate = :startDate
              AND c.endDate = :endDate
              AND c.warranty = :warranty
            """)
    Optional<CartItem> findMatchingItem(
            @Param("customerId") Long customerId,
            @Param("productId") Long productId,
            @Param("size") String size,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("warranty") String warranty
    );

    void deleteAllByCustomer_Id(Long customerId);
}
