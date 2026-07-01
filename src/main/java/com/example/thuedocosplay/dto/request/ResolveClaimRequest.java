package com.example.thuedocosplay.dto.request;

import com.example.thuedocosplay.entity.InsuranceClaim.ClaimStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class ResolveClaimRequest {

    @NotNull
    private ClaimStatus status;      // APPROVED | REJECTED

    // Bắt buộc khi APPROVED
    private BigDecimal approvedAmount;

    private String resolutionNote;   // Lý do duyệt/từ chối
}