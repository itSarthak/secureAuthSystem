package com.pipeline.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j

public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    @Async
    public void sendOTPEMail(String emailTo, String name, String otp, Instant expiresAt) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailTo);
            helper.setSubject(String.format("Your %s Verification Code", appName));

            String htmlContent = buildOTPEmailContent(name, otp, expiresAt);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", emailTo);

        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", emailTo, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private String buildOTPEmailContent(String name, String otp, Instant expiresAt) {
        String formattedTime = expiresAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4F46E5; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .otp-box { background-color: white; border: 2px solid #4F46E5; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }
                    .otp-code { font-size: 32px; font-weight: bold; color: #4F46E5; letter-spacing: 8px; }
                    .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #6b7280; }
                    .warning { background-color: #FEF3C7; border-left: 4px solid #F59E0B; padding: 12px; margin: 20px 0; border-radius: 4px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Thank you for signing up! Please use the verification code below to complete your registration:</p>
                        
                        <div class="otp-box">
                            <div class="otp-code">%s</div>
                        </div>
                        
                        <p><strong>This code will expire on %s</strong></p>
                        
                        <div class="warning">
                            <strong>⚠️ Security Note:</strong> Never share this code with anyone. Our team will never ask for your verification code.
                        </div>
                        
                        <p>If you didn't request this code, please ignore this email or contact our support team.</p>
                        
                        <div class="footer">
                            <p>&copy; 2024 %s. All rights reserved.</p>
                            <p>This is an automated message, please do not reply.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, appName, name, otp, formattedTime, appName);
    }


}
