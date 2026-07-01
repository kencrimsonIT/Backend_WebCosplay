package com.example.thuedocosplay.dto.response;

import com.example.thuedocosplay.entity.enums.VoucherAudience;
import com.example.thuedocosplay.entity.enums.VoucherDiscountType;
import com.example.thuedocosplay.entity.enums.VoucherProductScope;
import com.example.thuedocosplay.entity.enums.VoucherStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class VoucherResponse {
    private Long id;
    private String code;
    private String title;
    private Long sellerId;
    private String sellerName;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minimumOrderAmount;
    private Integer usageLimit;
    private Integer perUserLimit;
    private Integer usedCount;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private VoucherStatus status;
    private VoucherAudience audience;
    private VoucherProductScope productScope;
    private Boolean stackable;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
