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
    @NotBlank(message = "Vui long nhap ten chuong trinh")
    private String title;

    @NotBlank(message = "Vui long nhap ma voucher")
    private String code;

    @NotNull(message = "Vui long chon loai giam gia")
    private VoucherDiscountType discountType;

    @NotNull(message = "Vui long nhap gia tri giam")
    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    private Integer usageLimit;

    private Integer perUserLimit = 1;

    @NotNull(message = "Vui long chon ngay bat dau")
    private LocalDateTime startsAt;

    @NotNull(message = "Vui long chon ngay ket thuc")
    private LocalDateTime endsAt;

    private VoucherStatus status = VoucherStatus.ACTIVE;

    private VoucherAudience audience = VoucherAudience.ALL;

    private VoucherProductScope productScope = VoucherProductScope.SELLER_PRODUCTS;

    private Boolean stackable = false;

    private String description;
}
