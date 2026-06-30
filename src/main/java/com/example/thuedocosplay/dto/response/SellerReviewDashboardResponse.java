package com.example.thuedocosplay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SellerReviewDashboardResponse {
    private ReviewSummaryResponse summary;
    private List<ProductReviewResponse> reviews;
}
