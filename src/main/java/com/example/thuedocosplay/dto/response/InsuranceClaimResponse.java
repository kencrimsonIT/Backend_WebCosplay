package com.example.thuedocosplay.dto.response;

import com.example.thuedocosplay.entity.InsuranceClaim.ClaimStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class InsuranceClaimResponse {
    private Long id;
    private String claimCode;
    private String orderCode;
    private String customerName;
    private String customerPhone;
    private String planName;
    private BigDecimal planFee;
    private ClaimStatus status;
    private String description;
    private String evidenceUrls;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private String resolutionNote;
    private String resolvedByName;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}
