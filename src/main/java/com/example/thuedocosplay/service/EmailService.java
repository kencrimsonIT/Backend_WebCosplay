package com.example.thuedocosplay.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = "http://localhost:8080/api/v1/auth/verify?token=" + token;
        String subject = "Xác thực tài khoản WebCosplay";
        String content = "<p>Chào bạn,</p>"
                + "<p>Vui lòng nhấn vào link bên dưới để xác thực tài khoản của bạn:</p>"
                + "<a href=\"" + verificationUrl + "\">Xác thực ngay</a>"
                + "<br><p>Link này sẽ hết hạn sau 24h.</p>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi email: " + e.getMessage());
        }
    }
}
