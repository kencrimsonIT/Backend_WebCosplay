package com.example.thuedocosplay.dto.response;

import com.example.thuedocosplay.entity.enums.ReviewStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductReviewResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Long orderId;
    private String orderCode;
    private Long buyerId;
    private String buyerName;
    private Integer rating;
    private String content;
    private List<String> imageUrls;
    private ReviewStatus status;
    private String sellerResponse;
    private LocalDateTime sellerRespondedAt;
    private Long likeCount;
    private Boolean likedByCurrentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
