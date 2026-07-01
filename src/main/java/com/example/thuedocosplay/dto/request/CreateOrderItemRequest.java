package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderItemRequest {
    private Long productId;

    @NotBlank
    private String productName;

    @NotBlank
    private String categoryName;

    private String size;

    @NotNull
    @Min(1)
    private Integer days;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    private BigDecimal lineTotal;
}
