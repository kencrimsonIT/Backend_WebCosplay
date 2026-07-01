package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.UpdateProfileRequest;
import com.example.thuedocosplay.dto.response.UserResponse;
import com.example.thuedocosplay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Principal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            Principal principal,
            @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(principal.getName(), request));
    }

    @PostMapping("/avatar")
    public ResponseEntity<UserResponse> updateAvatar(
            Principal principal,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(userService.updateAvatar(principal.getName(), file));
    }
}
