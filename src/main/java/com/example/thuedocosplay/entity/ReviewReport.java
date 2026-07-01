package com.example.thuedocosplay.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Lưu lại từng lượt báo cáo (report) một đánh giá vi phạm.
 * Tách riêng khỏi Review để biết AI đã báo cáo, tránh báo cáo trùng lặp.
 *
 * Đặt tại: entity/ReviewReport.java
 */
@Entity
@Table(name = "review_reports",
        uniqueConstraints = @UniqueConstraint(columnNames = {"review_id", "reporter_id"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    // Lý do báo cáo: SPAM | OFFENSIVE | FAKE | OTHER
    @Column(nullable = false, length = 30)
    private String reason;

    @Column(length = 500)
    private String detail;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
