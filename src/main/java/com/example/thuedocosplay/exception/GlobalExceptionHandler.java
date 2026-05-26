package com.example.thuedocosplay.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        // Simple heuristic: if it contains Vietnamese characters or common business error patterns, it's likely a business exception
        if (ex.getMessage().contains("đã tồn tại") || ex.getMessage().contains("không khớp") || ex.getMessage().contains("không tìm thấy") || ex.getMessage().contains("Mã xác thực")) {
            return ResponseEntity.status(400).body(error);
        }
        return ResponseEntity.status(500).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Email hoặc mật khẩu không chính xác");
        return ResponseEntity.status(401).body(error);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, String>> handleDisabledException(DisabledException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Tài khoản chưa được xác thực email. Vui lòng kiểm tra hộp thư đến.");
        return ResponseEntity.status(403).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(400).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Đã có lỗi xảy ra trên hệ thống: " + ex.getMessage());
        return ResponseEntity.status(500).body(error);
    }
}
