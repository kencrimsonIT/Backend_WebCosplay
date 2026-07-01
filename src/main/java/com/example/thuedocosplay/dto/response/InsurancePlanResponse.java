// ═══ InsurancePlanResponse.java ═══════════════════════════════════════════
package com.example.thuedocosplay.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class InsurancePlanResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal feeAmount;
    private Integer coverPercent;
    private BigDecimal maxPayout;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
