// ═══════════════════════════════════════════════════════════════════════════
// FILE: entity/InsurancePlan.java (TẠO MỚI)
// ═══════════════════════════════════════════════════════════════════════════
package com.example.thuedocosplay.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Gói bảo hiểm được admin cấu hình.
 * Ví dụ: Cơ bản (30k/đơn), Tiêu chuẩn (60k), Cao cấp (100k)
 */
@Entity
@Table(name = "insurance_plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InsurancePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;                   // Cơ bản | Tiêu chuẩn | Cao cấp

    @Column(length = 500)
    private String description;

    @Column(name = "fee_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal feeAmount;          // Phí bảo hiểm (VND/đơn)

    // Bảo vệ tối đa bao nhiêu % số tiền cọc
    @Column(name = "cover_percent", nullable = false)
    private Integer coverPercent;          // 80 | 90 | 100

    // Giới hạn bồi hoàn tối đa (VND)
    @Column(name = "max_payout", precision = 15, scale = 0)
    private BigDecimal maxPayout;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }
}
