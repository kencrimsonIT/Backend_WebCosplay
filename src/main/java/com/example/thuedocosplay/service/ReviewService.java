package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.ReviewRequest;
import com.example.thuedocosplay.dto.response.ReviewResponse;
import com.example.thuedocosplay.entity.*;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import com.example.thuedocosplay.exception.ResourceNotFoundException;
import com.example.thuedocosplay.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final RentalOrderRepository orderRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Gửi đánh giá
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public ReviewResponse submitReview(String userEmail, ReviewRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        RentalOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        // Kiểm tra đơn hàng phải COMPLETED
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("Chỉ được đánh giá khi đơn hàng đã hoàn thành");
        }

        // Kiểm tra đơn hàng có chứa sản phẩm này không
        boolean productInOrder = order.getItems().stream()
                .anyMatch(item -> item.getProduct() != null
                        && item.getProduct().getId().equals(request.getProductId()));
        if (!productInOrder) {
            throw new IllegalStateException("Sản phẩm không có trong đơn hàng này");
        }

        // Kiểm tra đã đánh giá chưa
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new IllegalStateException("Bạn đã đánh giá sản phẩm này rồi");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .order(order)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return toResponse(reviewRepository.save(review));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lấy danh sách đánh giá của sản phẩm
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .productId(r.getProduct().getId())
                .productName(r.getProduct().getName())
                .userId(r.getUser().getId())
                .userName(r.getUser().getFullName())
                .userAvatar(r.getUser().getAvatarUrl())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
