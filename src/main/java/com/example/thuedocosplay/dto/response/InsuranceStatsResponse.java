package com.example.thuedocosplay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class InsuranceStatsResponse {
    private long pendingClaims;
    private long verifyingClaims;
    private long approvedClaims;
    private long rejectedClaims;
    private long totalClaims;
    private BigDecimal totalPaidOut;   // Tổng tiền đã bồi hoàn (tháng hiện tại)
}
