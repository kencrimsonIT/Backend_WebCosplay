package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SellerReviewResponseRequest {
    @NotBlank(message = "Vui long nhap phan hoi")
    private String response;
}
