package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Yêu cầu nhập họ và tên")
    @Size(max = 255)
    private String fullName;

    @NotBlank(message = "Yêu cầu nhập email")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Yêu cầu nhập mật khẩu")
    @Size(min = 8, max = 255, message = "Mật khẩu phải chứa ít nhất 8 ký tự")
    private String password;

    @NotBlank(message = "Yêu cầu xác nhận mật khẩu")
    private String confirmPassword;
}
