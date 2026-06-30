package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.CreateProductReviewRequest;
import com.example.thuedocosplay.dto.request.SellerReviewResponseRequest;
import com.example.thuedocosplay.dto.request.UpdateProductReviewRequest;
import com.example.thuedocosplay.dto.response.ProductReviewResponse;
import com.example.thuedocosplay.dto.response.ReviewSummaryResponse;
import com.example.thuedocosplay.dto.response.SellerReviewDashboardResponse;
import com.example.thuedocosplay.entity.Product;
import com.example.thuedocosplay.entity.ProductReviewLike;
import com.example.thuedocosplay.entity.ProductReview;
import com.example.thuedocosplay.entity.RentalOrder;
import com.example.thuedocosplay.entity.User;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import com.example.thuedocosplay.entity.enums.ReviewStatus;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.ProductRepository;
import com.example.thuedocosplay.repository.ProductReviewLikeRepository;
import com.example.thuedocosplay.repository.ProductReviewRepository;
import com.example.thuedocosplay.repository.RentalOrderRepository;
import com.example.thuedocosplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductReviewLikeRepository likeRepository;
    private final ProductRepository productRepository;
    private final RentalOrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ProductReviewResponse> listVisibleProductReviews(Long productId) {
        return reviewRepository.findAllByProduct_IdAndStatusOrderByCreatedAtDesc(productId, ReviewStatus.VISIBLE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse productSummary(Long productId) {
        long visible = reviewRepository.countByProduct_IdAndStatus(productId, ReviewStatus.VISIBLE);
        long hidden = reviewRepository.countByProduct_IdAndStatus(productId, ReviewStatus.HIDDEN);
        return ReviewSummaryResponse.builder()
                .totalReviews(visible + hidden)
                .visibleReviews(visible)
                .hiddenReviews(hidden)
                .pendingResponse(0)
                .lowRatingReviews(0)
                .averageRating(roundOne(reviewRepository.averageRatingByProduct(productId, ReviewStatus.VISIBLE)))
                .build();
    }

    @Transactional
    public ProductReviewResponse createReview(String currentUserEmail, CreateProductReviewRequest request) {
        User buyer = currentUser(currentUserEmail);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay san pham"));
        RentalOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay don hang"));

        boolean canReview = orderRepository.existsCompletedCustomerOrderContainingProduct(
                order.getId(),
                product.getId(),
                buyer.getId(),
                buyer.getEmail(),
                OrderStatus.COMPLETED
        );
        if (!canReview) {
            throw new AccessDeniedException("Chi co the danh gia san pham trong don hang da hoan thanh cua ban");
        }
        if (reviewRepository.existsByOrder_IdAndProduct_IdAndBuyer_Id(order.getId(), product.getId(), buyer.getId())) {
            throw new IllegalArgumentException("Ban da danh gia san pham nay trong don hang nay");
        }

        ProductReview review = ProductReview.builder()
                .product(product)
                .order(order)
                .buyer(buyer)
                .rating(request.getRating())
                .content(request.getContent().trim())
                .imageUrls(joinImageUrls(request.getImageUrls()))
                .status(ReviewStatus.VISIBLE)
                .build();

        ProductReview saved = reviewRepository.save(review);
        log.info("[ProductReview] buyer={} created review={} product={} order={}",
                buyer.getEmail(), saved.getId(), product.getId(), order.getOrderCode());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductReviewResponse> listMyReviews(String currentUserEmail) {
        User buyer = currentUser(currentUserEmail);
        return reviewRepository.findAllByBuyer_IdOrderByCreatedAtDesc(buyer.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductReviewResponse updateMyReview(String currentUserEmail, Long reviewId, UpdateProductReviewRequest request) {
        User buyer = currentUser(currentUserEmail);
        ProductReview review = reviewRepository.findByIdAndBuyer_Id(reviewId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay danh gia cua ban"));
        review.setRating(request.getRating());
        review.setContent(request.getContent().trim());
        review.setImageUrls(joinImageUrls(request.getImageUrls()));
        review.setStatus(ReviewStatus.VISIBLE);
        ProductReview saved = reviewRepository.save(review);
        log.info("[ProductReview] buyer={} updated review={}", buyer.getEmail(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void deleteMyReview(String currentUserEmail, Long reviewId) {
        User buyer = currentUser(currentUserEmail);
        ProductReview review = reviewRepository.findByIdAndBuyer_Id(reviewId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay danh gia cua ban"));
        reviewRepository.delete(review);
        log.info("[ProductReview] buyer={} deleted review={}", buyer.getEmail(), reviewId);
    }

    @Transactional(readOnly = true)
    public SellerReviewDashboardResponse listSellerReviews(String currentUserEmail, ReviewStatus status) {
        User seller = currentUser(currentUserEmail);
        List<ProductReviewResponse> reviews = reviewRepository.findSellerReviews(seller.getId(), status)
                .stream()
                .map(this::toResponse)
                .toList();
        return SellerReviewDashboardResponse.builder()
                .summary(sellerSummary(seller.getId()))
                .reviews(reviews)
                .build();
    }

    @Transactional
    public ProductReviewResponse respondAsSeller(String currentUserEmail, Long reviewId, SellerReviewResponseRequest request) {
        User seller = currentUser(currentUserEmail);
        ProductReview review = sellerReview(reviewId, seller.getId());
        review.setSellerResponse(request.getResponse().trim());
        review.setSellerRespondedAt(LocalDateTime.now());
        ProductReview saved = reviewRepository.save(review);
        log.info("[ProductReview] seller={} responded review={}", seller.getEmail(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public ProductReviewResponse setVisibilityAsSeller(String currentUserEmail, Long reviewId, ReviewStatus status) {
        User seller = currentUser(currentUserEmail);
        ProductReview review = sellerReview(reviewId, seller.getId());
        review.setStatus(status);
        ProductReview saved = reviewRepository.save(review);
        log.info("[ProductReview] seller={} set review={} status={}", seller.getEmail(), saved.getId(), status);
        return toResponse(saved);
    }

    @Transactional
    public ProductReviewResponse likeReview(String currentUserEmail, Long reviewId) {
        User user = currentUser(currentUserEmail);
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay danh gia"));
        if (!likeRepository.existsByReview_IdAndUser_Id(reviewId, user.getId())) {
            likeRepository.save(ProductReviewLike.builder()
                    .review(review)
                    .user(user)
                    .build());
            log.info("[ProductReview] user={} liked review={}", user.getEmail(), reviewId);
        }
        return toResponse(review, user.getId());
    }

    @Transactional
    public ProductReviewResponse unlikeReview(String currentUserEmail, Long reviewId) {
        User user = currentUser(currentUserEmail);
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay danh gia"));
        likeRepository.findByReview_IdAndUser_Id(reviewId, user.getId())
                .ifPresent(likeRepository::delete);
        log.info("[ProductReview] user={} unliked review={}", user.getEmail(), reviewId);
        return toResponse(review, user.getId());
    }

    public ProductReviewResponse toResponse(ProductReview review) {
        return toResponse(review, null);
    }

    private ProductReviewResponse toResponse(ProductReview review, Long currentUserId) {
        Product product = review.getProduct();
        RentalOrder order = review.getOrder();
        User buyer = review.getBuyer();
        return ProductReviewResponse.builder()
                .id(review.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(product.getImageUrl())
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .buyerId(buyer.getId())
                .buyerName(maskName(buyer.getFullName()))
                .rating(review.getRating())
                .content(review.getContent())
                .imageUrls(splitImageUrls(review.getImageUrls()))
                .status(review.getStatus())
                .sellerResponse(review.getSellerResponse())
                .sellerRespondedAt(review.getSellerRespondedAt())
                .likeCount(likeRepository.countByReview_Id(review.getId()))
                .likedByCurrentUser(currentUserId != null && likeRepository.existsByReview_IdAndUser_Id(review.getId(), currentUserId))
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private ReviewSummaryResponse sellerSummary(Long sellerId) {
        long total = reviewRepository.countByProduct_Seller_Id(sellerId);
        long visible = reviewRepository.countByProduct_Seller_IdAndStatus(sellerId, ReviewStatus.VISIBLE);
        long hidden = reviewRepository.countByProduct_Seller_IdAndStatus(sellerId, ReviewStatus.HIDDEN);
        long pendingResponse = reviewRepository.countByProduct_Seller_IdAndSellerResponseIsNull(sellerId);
        long lowRating = reviewRepository.countByProduct_Seller_IdAndRatingLessThanEqual(sellerId, 2);
        return ReviewSummaryResponse.builder()
                .totalReviews(total)
                .visibleReviews(visible)
                .hiddenReviews(hidden)
                .pendingResponse(pendingResponse)
                .lowRatingReviews(lowRating)
                .averageRating(roundOne(reviewRepository.averageRatingBySeller(sellerId, ReviewStatus.VISIBLE)))
                .build();
    }

    private ProductReview sellerReview(Long reviewId, Long sellerId) {
        return reviewRepository.findSellerReview(reviewId, sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay danh gia thuoc shop cua ban"));
    }

    private User currentUser(String email) {
        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            throw new AccessDeniedException("Vui long dang nhap");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung"));
    }

    private String joinImageUrls(List<String> urls) {
        if (urls == null) return null;
        String joined = urls.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .limit(5)
                .reduce((left, right) -> left + "\n" + right)
                .orElse(null);
        return joined == null || joined.length() <= 2000 ? joined : joined.substring(0, 2000);
    }

    private List<String> splitImageUrls(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split("\\R"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private String maskName(String name) {
        if (name == null || name.isBlank()) return "Khach hang";
        String trimmed = name.trim();
        if (trimmed.length() <= 2) return trimmed;
        return trimmed.charAt(0) + "***" + trimmed.charAt(trimmed.length() - 1);
    }

    private double roundOne(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
