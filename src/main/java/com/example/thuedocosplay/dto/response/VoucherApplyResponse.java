package com.example.thuedocosplay.dto.response;

import com.example.thuedocosplay.entity.enums.VoucherDiscountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VoucherApplyResponse {
    private Long voucherId;
    private String code;
    private String title;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal eligibleSubtotal;
    private BigDecimal discountAmount;
    private BigDecimal payableTotal;
    private Boolean stackable;
    private Integer usageLimit;
    private Integer perUserLimit;
    private Long usedCount;
    private Long userUsedCount;
    private String message;
}
