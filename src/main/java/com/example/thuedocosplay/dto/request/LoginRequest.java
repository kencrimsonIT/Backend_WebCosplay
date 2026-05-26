package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Yêu cầu nhập email")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Yêu cầu nhập mật khẩu")
    private String password;
}
