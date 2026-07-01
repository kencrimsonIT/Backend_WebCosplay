package com.example.thuedocosplay.entity;

import com.example.thuedocosplay.entity.enums.ModerationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity đánh giá sản phẩm.
 * Tạo bảng mới: reviews
 *
 * Ràng buộc:
 * - Mỗi user chỉ được đánh giá 1 lần cho 1 sản phẩm
 * - Chỉ được đánh giá nếu đơn hàng COMPLETED
 */
@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Đơn hàng gốc để xác minh đã thuê
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private RentalOrder order;

    // 1 - 5 sao
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false, length = 20)
    @Builder.Default
    private ModerationStatus moderationStatus = ModerationStatus.APPROVED;
    // Mặc định APPROVED để không breaking các review cũ đã có.
    // Có thể đổi default thành PENDING nếu muốn duyệt thủ công mọi review mới.

    @Column(name = "report_count", nullable = false)
    @Builder.Default
    private Integer reportCount = 0;
    // Số lần bị người dùng báo cáo

    @Column(name = "moderation_note", length = 500)
    private String moderationNote;
    // Lý do admin ẩn/từ chối (hiển thị nội bộ, không public)

    @Column(name = "moderated_at")
    private java.time.LocalDateTime moderatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderated_by")
    private User moderatedBy;
    // Admin nào đã xử lý

}
