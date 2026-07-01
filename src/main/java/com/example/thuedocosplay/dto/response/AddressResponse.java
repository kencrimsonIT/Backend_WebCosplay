package com.example.thuedocosplay.dto.response;

import com.example.thuedocosplay.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String fullAddress;
    private String label;
    private boolean isDefault;

    public static AddressResponse fromEntity(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullAddress(address.getFullAddress())
                .label(address.getLabel())
                .isDefault(address.isDefault())
                .build();
    }
}
