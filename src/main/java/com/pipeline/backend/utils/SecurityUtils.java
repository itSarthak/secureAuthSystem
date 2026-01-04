package com.pipeline.backend.utils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Component
public class SecurityUtils {

    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    private final AesCryptoUtil aesCryptoUtil;

    public SecurityUtils(AesCryptoUtil aesCryptoUtil) {
        this.aesCryptoUtil = aesCryptoUtil;
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        this.secureRandom = new SecureRandom();
    }

    public String saltAndEncryptID(String userId) {
        String salt = UUID.randomUUID().toString();
        String payload = userId + ":" + salt;
        return aesCryptoUtil.encrypt(payload);
    }

    public String generateOTP() {
        int otp = 100000 + secureRandom.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    public String hashOTP(String otp) {
        return hashWithSHA256(otp);
    }

    // Private methods
    private String hashWithSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
