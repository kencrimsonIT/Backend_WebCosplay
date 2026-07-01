package com.example.thuedocosplay.entity;

import com.example.thuedocosplay.entity.enums.VoucherAudience;
import com.example.thuedocosplay.entity.enums.VoucherDiscountType;
import com.example.thuedocosplay.entity.enums.VoucherProductScope;
import com.example.thuedocosplay.entity.enums.VoucherStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private VoucherDiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 15, scale = 0)
    private BigDecimal discountValue;

    @Column(name = "max_discount_amount", precision = 15, scale = 0)
    private BigDecimal maxDiscountAmount;

    @Column(name = "minimum_order_amount", nullable = false, precision = 15, scale = 0)
    @Builder.Default
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "per_user_limit")
    @Builder.Default
    private Integer perUserLimit = 1;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VoucherStatus status = VoucherStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VoucherAudience audience = VoucherAudience.ALL;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_scope", nullable = false)
    @Builder.Default
    private VoucherProductScope productScope = VoucherProductScope.SELLER_PRODUCTS;

    @Column(name = "stackable", nullable = false)
    @Builder.Default
    private Boolean stackable = false;

    @Column(length = 1000)
    private String description;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
