package com.example.thuedocosplay.dto.response;

import com.example.thuedocosplay.entity.Promotion;
import com.example.thuedocosplay.entity.enums.PromotionStatus;
import com.example.thuedocosplay.entity.enums.PromotionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PromotionResponse {
    private Long id;
    private String code;
    private String title;
    private PromotionType type;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private Integer maxUses;
    private Integer usedCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String targetAudience;
    private String applyTo;
    private String extraCondition;
    private PromotionStatus status;
    private LocalDateTime createdAt;

    public static PromotionResponse from(Promotion p) {
        return PromotionResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .title(p.getTitle())
                .type(p.getType())
                .value(p.getValue())
                .minOrderAmount(p.getMinOrderAmount())
                .maxUses(p.getMaxUses())
                .usedCount(p.getUsedCount())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .targetAudience(p.getTargetAudience())
                .applyTo(p.getApplyTo())
                .extraCondition(p.getExtraCondition())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
