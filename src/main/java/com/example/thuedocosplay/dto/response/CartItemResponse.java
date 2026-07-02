package com.example.thuedocosplay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CartItemResponse {
    private Long id;
    private String cartKey;
    private Long productId;
    private String name;
    private String image;
    private String category;
    private String size;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer days;
    private BigDecimal pricePerDay;
    private BigDecimal rentalPrice;
    private BigDecimal deposit;
    private String warranty;
    private BigDecimal warrantyFee;
    private Integer quantity;
    private Integer maxQuantity;
}
