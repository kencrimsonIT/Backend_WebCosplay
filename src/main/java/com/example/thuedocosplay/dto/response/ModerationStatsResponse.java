package com.example.thuedocosplay.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModerationStatsResponse {
    private long pendingCount;
    private long approvedCount;
    private long hiddenCount;
    private long flaggedCount;
    private long totalReports;
}
