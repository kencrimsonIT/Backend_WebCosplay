package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.RentalOrder;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RentalOrderRepository
        extends JpaRepository<RentalOrder, Long>, JpaSpecificationExecutor<RentalOrder> {

    Optional<RentalOrder> findByOrderCode(String orderCode);

    List<RentalOrder> findAllByOrderByCreatedAtDesc();

    // ─── Đếm đơn theo trạng thái + khoảng thời gian ──────────────────────────
    long countByStatusInAndCreatedAtBetween(
            List<OrderStatus> statuses,
            LocalDateTime from,
            LocalDateTime to
    );

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    // ─── Tổng doanh thu (grand_total) của các đơn đã paidAt trong khoảng ─────
    @Query("""
            SELECT COALESCE(SUM(o.grandTotal), 0)
            FROM RentalOrder o
            WHERE o.status IN :statuses
              AND o.paidAt IS NOT NULL
              AND o.paidAt >= :from
              AND o.paidAt < :to
            """)
    BigDecimal sumRevenue(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // ─── Doanh thu theo danh mục ───────────────────────────────────────────────
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

    // ─── Top sản phẩm theo doanh thu ──────────────────────────────────────────
    @Query("""
            SELECT oi.productName       AS productName,
                   oi.categoryName      AS categoryName,
                   SUM(oi.quantity)     AS totalQuantity,
                   SUM(oi.lineTotal)    AS totalRevenue
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.status IN :statuses
              AND o.paidAt IS NOT NULL
              AND o.paidAt >= :from
              AND o.paidAt < :to
            GROUP BY oi.productName, oi.categoryName
            ORDER BY totalRevenue DESC
            """)
    List<TopProductProjection> topProductsByRevenue(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("statuses") List<OrderStatus> statuses,
            Pageable pageable
    );

    // ─── Projections ───────────────────────────────────────────────────────────

    interface CategoryRevenueProjection {
        String getCategoryName();
        BigDecimal getRevenue();
    }

    interface TopProductProjection {
        String getProductName();
        String getCategoryName();
        Long getTotalQuantity();
        BigDecimal getTotalRevenue();
    }
}
