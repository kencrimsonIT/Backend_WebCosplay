package com.example.thuedocosplay.dto.response;

import com.example.thuedocosplay.entity.Promotion;
import com.example.thuedocosplay.entity.Voucher;
import com.example.thuedocosplay.entity.enums.PromotionStatus;
import com.example.thuedocosplay.entity.enums.PromotionType;
import com.example.thuedocosplay.entity.enums.VoucherDiscountType;
import com.example.thuedocosplay.entity.enums.VoucherStatus;
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

    public static PromotionResponse fromVoucher(Voucher voucher) {
        return PromotionResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .title(voucher.getTitle())
                .type(toPromotionType(voucher.getDiscountType()))
                .value(voucher.getDiscountValue())
                .minOrderAmount(voucher.getMinimumOrderAmount())
                .maxUses(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .startDate(voucher.getStartsAt() != null ? voucher.getStartsAt().toLocalDate() : null)
                .endDate(voucher.getEndsAt() != null ? voucher.getEndsAt().toLocalDate() : null)
                .targetAudience(voucher.getAudience() != null ? voucher.getAudience().name().toLowerCase() : "all")
                .applyTo(voucher.getProductScope() != null ? voucher.getProductScope().name().toLowerCase() : "seller_products")
                .extraCondition(voucher.getDescription())
                .status(toPromotionStatus(voucher.getStatus()))
                .createdAt(voucher.getCreatedAt())
                .build();
    }

    private static PromotionType toPromotionType(VoucherDiscountType type) {
        if (type == VoucherDiscountType.FIXED_AMOUNT) return PromotionType.AMOUNT;
        if (type == VoucherDiscountType.FREE_SHIPPING) return PromotionType.FREESHIP;
        return PromotionType.PERCENT;
    }

    private static PromotionStatus toPromotionStatus(VoucherStatus status) {
        if (status == VoucherStatus.PAUSED) return PromotionStatus.PAUSED;
        if (status == VoucherStatus.EXPIRED) return PromotionStatus.EXPIRED;
        if (status == VoucherStatus.DRAFT) return PromotionStatus.DRAFT;
        return PromotionStatus.ACTIVE;
    }
}
