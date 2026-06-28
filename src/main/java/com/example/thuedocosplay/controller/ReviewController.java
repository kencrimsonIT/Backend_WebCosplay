// ═══════════════════════════════════════════════════════════════════════════
// FILE 1: ReviewController.java
// ═══════════════════════════════════════════════════════════════════════════
package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.ReviewRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.ReviewResponse;
import com.example.thuedocosplay.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Đặt tại: controller/ReviewController.java
 *
 * Endpoints:
 *   POST /api/reviews              → gửi đánh giá (cần đăng nhập)
 *   GET  /api/reviews/product/{id} → lấy đánh giá của sản phẩm (public)
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ApiResponse<ReviewResponse> submitReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.ok(reviewService.submitReview(userDetails.getUsername(), request));
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<List<ReviewResponse>> getProductReviews(@PathVariable Long productId) {
        return ApiResponse.ok(reviewService.getReviewsByProduct(productId));
    }
}
