package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.ModerationActionRequest;
import com.example.thuedocosplay.dto.request.ReportReviewRequest;
import com.example.thuedocosplay.dto.response.ModerationReviewResponse;
import com.example.thuedocosplay.dto.response.ModerationStatsResponse;
import com.example.thuedocosplay.dto.response.PagedResponse;
import com.example.thuedocosplay.entity.Review;
import com.example.thuedocosplay.entity.ReviewReport;
import com.example.thuedocosplay.entity.SystemConfig;
import com.example.thuedocosplay.entity.User;
import com.example.thuedocosplay.entity.enums.ModerationStatus;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.ReviewReportRepository;
import com.example.thuedocosplay.repository.ReviewRepository;
import com.example.thuedocosplay.repository.SystemConfigRepository;
import com.example.thuedocosplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Đặt tại: service/ModerationService.java
 *
 * Xử lý kiểm duyệt nội dung đánh giá (Review):
 * - Admin duyệt / ẩn review
 * - User báo cáo (report) review vi phạm
 * - Thống kê số liệu kiểm duyệt
 */
@Service
@RequiredArgsConstructor
public class ModerationService {

    private final ReviewRepository reviewRepository;
    private final ReviewReportRepository reportRepository;
    private final UserRepository userRepository;
    private final SystemConfigRepository configRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ModerationReviewResponse> listReviews(
            ModerationStatus status, int page, int size) {

        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(size, 100),
                Sort.by("createdAt").descending()
        );

        Page<Review> result = status != null
                ? reviewRepository.findByModerationStatusOrderByCreatedAtDesc(status, pageable)
                : reviewRepository.findAllByOrderByCreatedAtDesc(pageable);

        return PagedResponse.of(result.map(this::toResponse));
    }

    @Transactional
    public ModerationReviewResponse moderate(Long reviewId, String adminEmail, ModerationActionRequest req) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá"));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy admin"));

        if (req.getStatus() == ModerationStatus.HIDDEN
                && (req.getNote() == null || req.getNote().isBlank())) {
            throw new IllegalArgumentException("Cần ghi lý do khi ẩn đánh giá");
        }

        review.setModerationStatus(req.getStatus());
        review.setModerationNote(req.getNote());
        review.setModeratedAt(LocalDateTime.now());
        review.setModeratedBy(admin);

        // Khi admin duyệt lại 1 review đã bị report → reset report count
        if (req.getStatus() == ModerationStatus.APPROVED) {
            review.setReportCount(0);
        }

        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void reportReview(Long reviewId, String userEmail, ReportReviewRequest req) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá"));

        User reporter = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (reportRepository.existsByReviewIdAndReporterId(reviewId, reporter.getId())) {
            throw new IllegalStateException("Bạn đã báo cáo đánh giá này rồi");
        }

        ReviewReport report = ReviewReport.builder()
                .review(review)
                .reporter(reporter)
                .reason(req.getReason())
                .detail(req.getDetail())
                .build();
        reportRepository.save(report);

        // Tăng report count, tự động chuyển sang FLAGGED nếu vượt ngưỡng
        review.setReportCount(review.getReportCount() + 1);
        if (review.getReportCount() >= 3 && review.getModerationStatus() == ModerationStatus.APPROVED) {
            review.setModerationStatus(ModerationStatus.FLAGGED);
        }
        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public ModerationStatsResponse getStats() {
        long pending  = reviewRepository.countByModerationStatus(ModerationStatus.PENDING);
        long approved = reviewRepository.countByModerationStatus(ModerationStatus.APPROVED);
        long hidden   = reviewRepository.countByModerationStatus(ModerationStatus.HIDDEN);
        long flagged  = reviewRepository.countByModerationStatus(ModerationStatus.FLAGGED);

        return ModerationStatsResponse.builder()
                .pendingCount(pending)
                .approvedCount(approved)
                .hiddenCount(hidden)
                .flaggedCount(flagged)
                .totalReports(pending + approved + hidden + flagged)
                .build();
    }
    @Transactional(readOnly = true)
    public String getBannedKeywords() {
        return configRepository.findById("BANNED_KEYWORDS")
                .map(SystemConfig::getValue)
                .orElse("***, ***");
    }

    @Transactional
    public void updateBannedKeywords(String keywords) {
        SystemConfig config = configRepository.findById("BANNED_KEYWORDS")
                .orElse(new SystemConfig("BANNED_KEYWORDS", "", LocalDateTime.now()));

        config.setValue(keywords);
        config.setUpdatedAt(LocalDateTime.now());
        configRepository.save(config);
    }

    // Hàm này dùng để quét bình luận TỰ ĐỘNG
    @Transactional(readOnly = true)
    public ModerationStatus autoFilterComment(String comment) {
        if (comment == null || comment.isBlank()) return ModerationStatus.APPROVED;

        String bannedStr = getBannedKeywords();
        if (bannedStr == null || bannedStr.isBlank()) return ModerationStatus.APPROVED;

        String[] bannedWords = bannedStr.split(",");
        String lowerComment = comment.toLowerCase();

        for (String word : bannedWords) {
            String cleanWord = word.trim().toLowerCase();
            if (!cleanWord.isEmpty() && lowerComment.contains(cleanWord)) {
                return ModerationStatus.HIDDEN; // Tự động ẨN nếu dính từ cấm
            }
        }
        return ModerationStatus.APPROVED;
    }


    private ModerationReviewResponse toResponse(Review r) {
        return ModerationReviewResponse.builder()
                .id(r.getId())
                .productId(r.getProduct().getId())
                .productName(r.getProduct().getName())
                .productImageUrl(r.getProduct().getImageUrl())
                .userId(r.getUser().getId())
                .userName(r.getUser().getFullName())
                .userAvatar(r.getUser().getAvatarUrl())
                .rating(r.getRating())
                .comment(r.getComment())
                .moderationStatus(r.getModerationStatus())
                .reportCount(r.getReportCount())
                .moderationNote(r.getModerationNote())
                .createdAt(r.getCreatedAt())
                .moderatedAt(r.getModeratedAt())
                .moderatedByName(r.getModeratedBy() != null ? r.getModeratedBy().getFullName() : null)
                .build();
    }
}
