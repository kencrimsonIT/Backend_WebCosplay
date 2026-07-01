package com.example.thuedocosplay.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewSummaryResponse {
    private long totalReviews;
    private long visibleReviews;
    private long hiddenReviews;
    private long pendingResponse;
    private long lowRatingReviews;
    private double averageRating;
}
