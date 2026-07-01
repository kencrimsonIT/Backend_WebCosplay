package com.example.thuedocosplay.repository;

import com.example.thuedocosplay.entity.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {

    boolean existsByReviewIdAndReporterId(Long reviewId, Long reporterId);

    List<ReviewReport> findByReviewIdOrderByCreatedAtDesc(Long reviewId);
}