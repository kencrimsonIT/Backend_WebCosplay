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

        sendEmail(to, subject, content);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = "http://localhost:3000/reset-password?token=" + token;
        String subject = "Khôi phục mật khẩu WebCosplay";
        String content = "<p>Chào bạn,</p>"
                + "<p>Bạn đã yêu cầu khôi phục mật khẩu. Vui lòng nhấn vào link bên dưới để đặt lại mật khẩu:</p>"
                + "<a href=\"" + resetUrl + "\">Đặt lại mật khẩu</a>"
                + "<br><p>Link này sẽ hết hạn sau 45 phút.</p>"
                + "<p>Nếu bạn không yêu cầu này, vui lòng bỏ qua email này.</p>";

        sendEmail(to, subject, content);
    }

    private void sendEmail(String to, String subject, String content) {
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
