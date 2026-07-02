package com.example.thuedocosplay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private List<CartItemResponse> items;
    private Integer itemCount;
    private BigDecimal rentalTotal;
    private BigDecimal warrantyTotal;
    private BigDecimal depositTotal;
    private BigDecimal total;
}
