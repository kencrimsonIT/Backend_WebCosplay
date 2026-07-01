package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.CreateProductReviewRequest;
import com.example.thuedocosplay.dto.request.SellerReviewResponseRequest;
import com.example.thuedocosplay.dto.request.UpdateProductReviewRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.ProductReviewResponse;
import com.example.thuedocosplay.dto.response.ReviewSummaryResponse;
import com.example.thuedocosplay.dto.response.SellerReviewDashboardResponse;
import com.example.thuedocosplay.entity.enums.ReviewStatus;
import com.example.thuedocosplay.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews") // Đặt đường dẫn gốc chung tại đây
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService reviewService;

    // Lấy đánh giá sản phẩm (Public)
    @GetMapping("/product/{productId}")
    public ApiResponse<List<ProductReviewResponse>> listProductReviews(@PathVariable Long productId) {
        return ApiResponse.ok(reviewService.listVisibleProductReviews(productId));
    }

    @GetMapping("/product/{productId}/summary")
    public ApiResponse<ReviewSummaryResponse> productReviewSummary(@PathVariable Long productId) {
        return ApiResponse.ok(reviewService.productSummary(productId));
    }

    // Gửi đánh giá (Cần đăng nhập)
    @PostMapping
    public ApiResponse<ProductReviewResponse> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateProductReviewRequest request
    ) {
        return ApiResponse.ok("Đã gửi đánh giá", reviewService.createReview(userDetails.getUsername(), request));
    }

    // Quản lý đánh giá cá nhân
    @GetMapping("/my")
    public ApiResponse<List<ProductReviewResponse>> myReviews(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(reviewService.listMyReviews(userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductReviewResponse> updateMyReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductReviewRequest request
    ) {
        return ApiResponse.ok("Đã cập nhật đánh giá", reviewService.updateMyReview(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMyReview(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        reviewService.deleteMyReview(userDetails.getUsername(), id);
        return ApiResponse.ok("Đã xóa đánh giá", null);
    }

    // Like/Unlike
    @PostMapping("/{id}/like")
    public ApiResponse<ProductReviewResponse> likeReview(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        return ApiResponse.ok("Đã thích đánh giá", reviewService.likeReview(userDetails.getUsername(), id));
    }

    @DeleteMapping("/{id}/like")
    public ApiResponse<ProductReviewResponse> unlikeReview(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        return ApiResponse.ok("Đã bỏ thích đánh giá", reviewService.unlikeReview(userDetails.getUsername(), id));
    }

    // Quản lý dành cho người bán (Seller)
    @GetMapping("/seller")
    public ApiResponse<SellerReviewDashboardResponse> sellerReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) ReviewStatus status
    ) {
        return ApiResponse.ok(reviewService.listSellerReviews(userDetails.getUsername(), status));
    }

    @PostMapping("/seller/{id}/response")
    public ApiResponse<ProductReviewResponse> respondAsSeller(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody SellerReviewResponseRequest request
    ) {
        return ApiResponse.ok("Đã phản hồi đánh giá", reviewService.respondAsSeller(userDetails.getUsername(), id, request));
    }

    @PatchMapping("/seller/{id}/visibility")
    public ApiResponse<ProductReviewResponse> setReviewVisibility(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam ReviewStatus status
    ) {
        return ApiResponse.ok("Đã cập nhật trạng thái đánh giá", reviewService.setVisibilityAsSeller(userDetails.getUsername(), id, status));
    }
}