package com.example.thuedocosplay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionSummaryResponse {
    private long activeCount;
    private long totalUsed;
    private long expiringSoon;
}
