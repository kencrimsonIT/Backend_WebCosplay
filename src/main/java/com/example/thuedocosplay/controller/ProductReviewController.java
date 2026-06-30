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
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService reviewService;

    @GetMapping("/api/products/{productId}/reviews")
    public ApiResponse<List<ProductReviewResponse>> listProductReviews(@PathVariable Long productId) {
        return ApiResponse.ok(reviewService.listVisibleProductReviews(productId));
    }

    @GetMapping("/api/products/{productId}/reviews/summary")
    public ApiResponse<ReviewSummaryResponse> productReviewSummary(@PathVariable Long productId) {
        return ApiResponse.ok(reviewService.productSummary(productId));
    }

    @PostMapping("/api/reviews")
    public ApiResponse<ProductReviewResponse> createReview(
            Principal principal,
            @Valid @RequestBody CreateProductReviewRequest request
    ) {
        return ApiResponse.ok("Da gui danh gia", reviewService.createReview(currentEmail(principal), request));
    }

    @GetMapping("/api/reviews/my")
    public ApiResponse<List<ProductReviewResponse>> myReviews(Principal principal) {
        return ApiResponse.ok(reviewService.listMyReviews(currentEmail(principal)));
    }

    @PutMapping("/api/reviews/{id}")
    public ApiResponse<ProductReviewResponse> updateMyReview(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductReviewRequest request
    ) {
        return ApiResponse.ok("Da cap nhat danh gia", reviewService.updateMyReview(currentEmail(principal), id, request));
    }

    @DeleteMapping("/api/reviews/{id}")
    public ApiResponse<Void> deleteMyReview(Principal principal, @PathVariable Long id) {
        reviewService.deleteMyReview(currentEmail(principal), id);
        return ApiResponse.ok("Da xoa danh gia", null);
    }

    @PostMapping("/api/reviews/{id}/like")
    public ApiResponse<ProductReviewResponse> likeReview(Principal principal, @PathVariable Long id) {
        return ApiResponse.ok("Da thich danh gia", reviewService.likeReview(currentEmail(principal), id));
    }

    @DeleteMapping("/api/reviews/{id}/like")
    public ApiResponse<ProductReviewResponse> unlikeReview(Principal principal, @PathVariable Long id) {
        return ApiResponse.ok("Da bo thich danh gia", reviewService.unlikeReview(currentEmail(principal), id));
    }

    @GetMapping("/api/seller/reviews")
    public ApiResponse<SellerReviewDashboardResponse> sellerReviews(
            Principal principal,
            @RequestParam(required = false) ReviewStatus status
    ) {
        return ApiResponse.ok(reviewService.listSellerReviews(currentEmail(principal), status));
    }

    @PostMapping("/api/seller/reviews/{id}/response")
    public ApiResponse<ProductReviewResponse> respondAsSeller(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody SellerReviewResponseRequest request
    ) {
        return ApiResponse.ok("Da phan hoi danh gia", reviewService.respondAsSeller(currentEmail(principal), id, request));
    }

    @PatchMapping("/api/seller/reviews/{id}/visibility")
    public ApiResponse<ProductReviewResponse> setReviewVisibility(
            Principal principal,
            @PathVariable Long id,
            @RequestParam ReviewStatus status
    ) {
        return ApiResponse.ok("Da cap nhat trang thai danh gia", reviewService.setVisibilityAsSeller(currentEmail(principal), id, status));
    }

    private String currentEmail(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}
