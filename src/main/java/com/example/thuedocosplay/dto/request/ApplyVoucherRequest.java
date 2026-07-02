package com.example.thuedocosplay.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ApplyVoucherRequest {
    @NotBlank(message = "Vui lòng nhập mã voucher")
    private String code;

    @NotNull(message = "Thieu tong tien thue")
    private BigDecimal rentalTotal;

    private BigDecimal warrantyTotal = BigDecimal.ZERO;

    private BigDecimal depositTotal = BigDecimal.ZERO;

    @NotEmpty(message = "Gio hang dang trong")
    @Valid
    private List<CreateOrderItemRequest> items;
}
