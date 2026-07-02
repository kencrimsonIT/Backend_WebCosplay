package com.example.thuedocosplay.dto.request;

import com.example.thuedocosplay.entity.enums.VoucherAudience;
import com.example.thuedocosplay.entity.enums.VoucherDiscountType;
import com.example.thuedocosplay.entity.enums.VoucherProductScope;
import com.example.thuedocosplay.entity.enums.VoucherStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpsertVoucherRequest {
    @NotBlank(message = "Vui lòng nhập tên chương trình")
    private String title;

    @NotBlank(message = "Vui lòng nhập mã voucher")
    private String code;

    @NotNull(message = "Vui lòng chọn loại giảm giá")
    private VoucherDiscountType discountType;

    @NotNull(message = "Vui lòng nhập giá trị giảm")
    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    private Integer usageLimit;

    private Integer perUserLimit = 1;

    @NotNull(message = "Vui lòng chọn ngày bắt đầu")
    private LocalDateTime startsAt;

    @NotNull(message = "Vui lòng chọn ngày kết thúc")
    private LocalDateTime endsAt;

    private VoucherStatus status = VoucherStatus.ACTIVE;

    private VoucherAudience audience = VoucherAudience.ALL;

    private VoucherProductScope productScope = VoucherProductScope.SELLER_PRODUCTS;

    private Boolean stackable = false;

    private String description;
}
