package com.example.thuedocosplay.dto.request;

import com.example.thuedocosplay.entity.enums.ProductInventoryStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpsertProductRequest {
    @NotBlank
    private String name;

    @NotNull
    private Long categoryId;

    private Long sellerId;

    private String description;

    @NotNull
    @DecimalMin("0")
    private BigDecimal pricePerDay;

    @NotNull
    @DecimalMin("0")
    private BigDecimal deposit;

    private String imageUrl;

    private Boolean visible;

    @Min(0)
    private Integer quantity;

    private ProductInventoryStatus inventoryStatus;
}
