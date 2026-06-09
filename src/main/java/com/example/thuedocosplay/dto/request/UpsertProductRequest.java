package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpsertProductRequest {
    @NotBlank
    private String name;

    @NotNull
    private Long categoryId;

    @NotNull
    private BigDecimal pricePerDay;

    @NotNull
    private BigDecimal deposit;

    private String imageUrl;

    private Boolean visible;
}
