package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.RentalOrder;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {
    Optional<RentalOrder> findByOrderCode(String orderCode);

    List<RentalOrder> findAllByOrderByCreatedAtDesc();

    @Query("""
            SELECT oi.categoryName AS categoryName, SUM(oi.lineTotal) AS revenue
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.status IN :statuses
              AND o.paidAt IS NOT NULL
              AND o.paidAt >= :from
              AND o.paidAt < :to
            GROUP BY oi.categoryName
            ORDER BY revenue DESC
            """)
    List<CategoryRevenueProjection> sumRevenueByCategoryInMonth(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("statuses") List<OrderStatus> statuses
    );

    interface CategoryRevenueProjection {
        String getCategoryName();
        java.math.BigDecimal getRevenue();
    }
}
