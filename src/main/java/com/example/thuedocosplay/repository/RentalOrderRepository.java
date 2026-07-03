package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.RentalOrder;
import com.example.thuedocosplay.entity.User;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RentalOrderRepository
        extends JpaRepository<RentalOrder, Long>, JpaSpecificationExecutor<RentalOrder> {

    Optional<RentalOrder> findByOrderCode(String orderCode);

    List<RentalOrder> findAllByOrderByCreatedAtDesc();

    @Query("""
            SELECT o FROM RentalOrder o
            WHERE o.customer = :user OR o.customerEmail = :email
            ORDER BY o.createdAt DESC
            """)
    List<RentalOrder> findByCustomerOrEmail(
            @Param("user") User user,
            @Param("email") String email
    );

    long countByStatusInAndCreatedAtBetween(
            List<OrderStatus> statuses,
            LocalDateTime from,
            LocalDateTime to
    );

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

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

    @Query("""
            SELECT o
            FROM RentalOrder o
            WHERE o.status IN :statuses
              AND o.paidAt IS NOT NULL
              AND o.paidAt >= :from
              AND o.paidAt < :to
            ORDER BY o.paidAt ASC
            """)
    List<RentalOrder> findRevenueOrders(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("statuses") List<OrderStatus> statuses
    );

    // ─── ĐÃ XÓA KEYWORD Ở ĐÂY ────────────────────────────────────────────────
    @Query("""
            SELECT DISTINCT o
            FROM RentalOrder o
            JOIN o.items sellerItem
            JOIN sellerItem.product sellerProduct
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.product p
            WHERE sellerProduct.seller.id = :sellerId
              AND (:status IS NULL OR o.status = :status)
              AND (:fromDate IS NULL OR o.rentFrom >= :fromDate)
              AND (:toDate IS NULL OR o.rentTo <= :toDate)
            ORDER BY o.createdAt DESC
            """)
    List<RentalOrder> findSellerOrders(
            @Param("sellerId") Long sellerId,
            @Param("status") OrderStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT DISTINCT o
            FROM RentalOrder o
            JOIN o.items sellerItem
            JOIN sellerItem.product sellerProduct
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.product p
            WHERE o.id = :orderId
              AND sellerProduct.seller.id = :sellerId
            """)
    Optional<RentalOrder> findSellerOrderDetail(
            @Param("orderId") Long orderId,
            @Param("sellerId") Long sellerId
    );

    // Fallback: tìm theo orderId không cần seller (dùng khi sản phẩm không gán seller)
    @Query("""
            SELECT DISTINCT o
            FROM RentalOrder o
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.product p
            WHERE o.id = :orderId
            """)
    Optional<RentalOrder> findOrderDetailById(@Param("orderId") Long orderId);

    @Query("""
            SELECT DISTINCT o
            FROM RentalOrder o
            JOIN o.items sellerItem
            JOIN sellerItem.product sellerProduct
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.product p
            WHERE sellerProduct.seller.id = :sellerId
              AND o.status IN :statuses
              AND o.paidAt IS NOT NULL
              AND o.paidAt >= :from
              AND o.paidAt < :to
            ORDER BY o.paidAt ASC
            """)
    List<RentalOrder> findSellerRevenueOrders(
            @Param("sellerId") Long sellerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("statuses") List<OrderStatus> statuses
    );

    @Query("""
            SELECT DISTINCT o
            FROM RentalOrder o
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.product p
            WHERE (
                (o.customer IS NOT NULL AND o.customer.id = :customerId)
                OR LOWER(o.customerEmail) = LOWER(:email)
            )
              AND (:status IS NULL OR o.status = :status)
              AND (:fromDate IS NULL OR o.rentFrom >= :fromDate)
              AND (:toDate IS NULL OR o.rentTo <= :toDate)
            ORDER BY o.createdAt DESC
            """)
    List<RentalOrder> findCustomerHistory(
            @Param("customerId") Long customerId,
            @Param("email") String email,
            @Param("status") OrderStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT DISTINCT o
            FROM RentalOrder o
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.product p
            WHERE o.id = :id
              AND (
                (o.customer IS NOT NULL AND o.customer.id = :customerId)
                OR LOWER(o.customerEmail) = LOWER(:email)
              )
            """)
    Optional<RentalOrder> findCustomerOrderDetail(
            @Param("id") Long id,
            @Param("customerId") Long customerId,
            @Param("email") String email
    );

    @Query("""
            SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END
            FROM RentalOrder o
            JOIN o.items oi
            WHERE o.id = :orderId
              AND oi.product.id = :productId
              AND o.status = :status
              AND (
                (o.customer IS NOT NULL AND o.customer.id = :customerId)
                OR LOWER(o.customerEmail) = LOWER(:email)
              )
            """)
    boolean existsCompletedCustomerOrderContainingProduct(
            @Param("orderId") Long orderId,
            @Param("productId") Long productId,
            @Param("customerId") Long customerId,
            @Param("email") String email,
            @Param("status") OrderStatus status
    );

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