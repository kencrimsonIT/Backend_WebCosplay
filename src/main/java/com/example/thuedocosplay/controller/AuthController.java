package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.ForgotPasswordRequest;
import com.example.thuedocosplay.dto.request.ResetPasswordRequest;
import com.example.thuedocosplay.dto.request.ChangePasswordRequest;
import com.example.thuedocosplay.dto.request.LoginRequest;
import com.example.thuedocosplay.dto.request.RegisterRequest;
import com.example.thuedocosplay.dto.response.AuthResponse;
import com.example.thuedocosplay.dto.response.UserResponse;
import com.example.thuedocosplay.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok("Vui lòng kiểm tra email để khôi phục mật khẩu");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công");
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> oauth2Success(@AuthenticationPrincipal OAuth2User oauth2User) {
        return ResponseEntity.ok(authService.handleOAuth2Login(oauth2User));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.getMe(userDetails.getUsername()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        authService.changePassword(request, userDetails.getUsername());
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }
}
