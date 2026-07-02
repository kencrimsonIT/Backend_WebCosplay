package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CartItemRequest {
    @NotNull
    private Long productId;

    private String size;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    @Min(1)
    private Integer days;

    @Min(1)
    private Integer quantity = 1;

    private String warranty = "none";

    private BigDecimal warrantyFee = BigDecimal.ZERO;
}
