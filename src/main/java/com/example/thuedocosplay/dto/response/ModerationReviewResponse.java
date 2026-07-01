package com.example.thuedocosplay.dto.response;

import com.example.thuedocosplay.entity.enums.ModerationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ModerationReviewResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Integer rating;
    private String comment;
    private ModerationStatus moderationStatus;
    private Integer reportCount;
    private String moderationNote;
    private LocalDateTime createdAt;
    private LocalDateTime moderatedAt;
    private String moderatedByName;
}
