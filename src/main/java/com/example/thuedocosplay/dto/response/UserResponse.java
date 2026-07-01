package com.example.thuedocosplay.dto.response;

import com.example.thuedocosplay.entity.User;
import com.example.thuedocosplay.entity.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String avatarUrl;
    private UserRole role;
    private java.util.List<AddressResponse> addresses;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .addresses(user.getAddresses() != null ? 
                    user.getAddresses().stream().map(AddressResponse::fromEntity).collect(java.util.stream.Collectors.toList()) : null)
                .build();
    }
}
