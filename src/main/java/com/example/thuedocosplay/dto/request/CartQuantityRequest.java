package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartQuantityRequest {
    @NotNull
    @Min(1)
    private Integer quantity;
}
