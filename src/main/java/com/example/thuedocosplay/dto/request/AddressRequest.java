package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    @NotBlank(message = "Địa chỉ không được để trống")
    private String fullAddress;
    
    private String label; // e.g., Nhà riêng, Công ty
    
    private boolean isDefault;
}
