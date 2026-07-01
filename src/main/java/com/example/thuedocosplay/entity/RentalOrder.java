package com.example.thuedocosplay.entity;

import com.example.thuedocosplay.entity.enums.OrderStatus;
import com.example.thuedocosplay.entity.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rental_orders")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true, length = 32)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "rental_total", nullable = false, precision = 15, scale = 0)
    private BigDecimal rentalTotal;

    @Column(name = "warranty_total", nullable = false, precision = 15, scale = 0)
    private BigDecimal warrantyTotal;

    @Column(name = "deposit_total", nullable = false, precision = 15, scale = 0)
    private BigDecimal depositTotal;

    @Column(name = "discount_total", nullable = false, precision = 15, scale = 0)
    @Builder.Default
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Column(name = "voucher_code", length = 40)
    private String voucherCode;

    @Column(name = "voucher_title")
    private String voucherTitle;

    @Column(name = "grand_total", nullable = false, precision = 15, scale = 0)
    private BigDecimal grandTotal;

    @Column(name = "promotion_code", length = 50)
    private String promotionCode;

    @Column(name = "rent_from")
    private LocalDate rentFrom;

    @Column(name = "rent_to")
    private LocalDate rentTo;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
