package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.request.ForgotPasswordRequest;
import com.example.thuedocosplay.dto.request.ResetPasswordRequest;
import com.example.thuedocosplay.dto.request.ChangePasswordRequest;
import com.example.thuedocosplay.dto.request.LoginRequest;
import com.example.thuedocosplay.dto.request.RegisterRequest;
import com.example.thuedocosplay.dto.response.AuthResponse;
import com.example.thuedocosplay.dto.response.UserResponse;
import com.example.thuedocosplay.entity.User;
import com.example.thuedocosplay.repository.UserRepository;
import com.example.thuedocosplay.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${application.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Map<String, Object> claims = Map.of(
                "fullName", request.getFullName(),
                "email", request.getEmail(),
                "password", passwordEncoder.encode(request.getPassword())
        );

        String token = jwtService.generateVerificationToken(claims);
        emailService.sendVerificationEmail(request.getEmail(), token);

        return AuthResponse.builder()
                .accessToken(null)
                .expiresIn(0)
                .user(UserResponse.builder()
                        .fullName(request.getFullName())
                        .email(request.getEmail())
                        .role(User.UserRole.CLIENT)
                        .build())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (DisabledException e) {
            throw new RuntimeException("Tài khoản chưa được xác thực email");
        }

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        var jwtToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .expiresIn(accessTokenExpiration)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    public String verifyEmail(String token) {
        try {
            Claims claims = jwtService.extractAllClaims(token);
            String email = claims.get("email", String.class);
            String fullName = claims.get("fullName", String.class);
            String password = claims.get("password", String.class);

            if (userRepository.findByEmail(email).isPresent()) {
                return "Tài khoản đã được xác thực trước đo đó.";
            }

            var user = User.builder()
                    .fullName(fullName)
                    .email(email)
                    .password(password)
                    .role(User.UserRole.CLIENT)
                    .enabled(true)
                    .build();

            userRepository.save(user);

            return "Xác thực email thành công! Bạn có thể đăng nhập ngay bây giờ.";
        } catch (Exception e) {
            throw new RuntimeException("Mã xác thực không hợp lệ hoặc đã hết hạn");
        }
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email này"));

        Map<String, Object> claims = Map.of(
                "email", user.getEmail(),
                "passwordHash", user.getPassword()
        );

        String token = jwtService.generatePasswordResetToken(claims);
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        try {
            Claims claims = jwtService.extractAllClaims(request.getToken());
            if (!"password-reset".equals(claims.getSubject())) {
                throw new RuntimeException("Mã xác thực không hợp lệ");
            }
            String email = claims.get("email", String.class);
            String passwordHash = claims.get("passwordHash", String.class);

            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            if (!user.getPassword().equals(passwordHash)) {
                throw new RuntimeException("Mã xác thực đã được sử dụng hoặc không còn hiệu lực");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
        } catch (Exception e) {
            if (e instanceof RuntimeException && e.getMessage().contains("Mã xác thực")) {
                throw e;
            }
            throw new RuntimeException("Mã xác thực không hợp lệ hoặc đã hết hạn");
        }
    }

//    public void logout(String email) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
//
//    }

    @Transactional
    public AuthResponse handleOAuth2Login(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String fullName = oauth2User.getAttribute("name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .fullName(fullName)
                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .role(User.UserRole.CLIENT)
                                .enabled(true)
                                .build()
                ));

        String jwtToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .expiresIn(accessTokenExpiration)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    public UserResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return UserResponse.fromEntity(user);
    }

    public void changePassword(ChangePasswordRequest request, String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
