package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.RentalOrder;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {
    Optional<RentalOrder> findByOrderCode(String orderCode);

    List<RentalOrder> findAllByOrderByCreatedAtDesc();

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

    interface CategoryRevenueProjection {
        String getCategoryName();
        java.math.BigDecimal getRevenue();
    }
}
