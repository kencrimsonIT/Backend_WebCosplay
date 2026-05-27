package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Yêu cầu nhập mật khẩu hiện tại")
    private String currentPassword;

    @NotBlank(message = "Yêu cầu nhập mật khẩu mới")
    @Size(min = 8, max = 255, message = "Mật khẩu phải chứa ít nhất 8 ký tự")
    private String newPassword;

    @NotBlank(message = "Yêu cầu xác nhận mật khẩu")
    private String confirmNewPassword;
}
