package com.example.thuedocosplay.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Yêu cầu bồi thường bảo hiểm khi khách làm hỏng/mất đồ.
 * Trạng thái: PENDING → VERIFYING → APPROVED | REJECTED
 */
@Entity
@Table(name = "insurance_claims")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InsuranceClaim {

    public enum ClaimStatus {
        PENDING,     // Vừa gửi yêu cầu
        VERIFYING,   // Admin đang xác minh
        APPROVED,    // Đã duyệt bồi hoàn
        REJECTED     // Từ chối
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_code", unique = true, nullable = false, length = 20)
    private String claimCode;              // CLM-2026-xxxxx

    // Đơn hàng gốc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private RentalOrder order;

    // Gói bảo hiểm đã mua
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private InsurancePlan plan;

    // Người gửi claim (admin/staff mở thay mặt khách hoặc tự động)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.PENDING;

    // Mô tả sự cố
    @Column(nullable = false, length = 1000)
    private String description;

    // Bằng chứng (URL ảnh, cách nhau bởi dấu phẩy)
    @Column(name = "evidence_urls", length = 2000)
    private String evidenceUrls;

    // Số tiền khách đề nghị bồi hoàn
    @Column(name = "requested_amount", precision = 15, scale = 0)
    private BigDecimal requestedAmount;

    // Số tiền admin thực tế duyệt
    @Column(name = "approved_amount", precision = 15, scale = 0)
    private BigDecimal approvedAmount;

    // Admin xử lý
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
