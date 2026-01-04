package com.pipeline.backend.service;

import com.pipeline.backend.dto.SignupInitialRequestDTO;
import com.pipeline.backend.dto.SignupInitialResponseDTO;
import com.pipeline.backend.entity.authCode.AuthCode;
import com.pipeline.backend.entity.authCode.CodeType;
import com.pipeline.backend.entity.user.Provider;
import com.pipeline.backend.entity.user.Status;
import com.pipeline.backend.entity.user.User;
import com.pipeline.backend.exceptions.AuthenticationException;
import com.pipeline.backend.repository.AuthRepository;
import com.pipeline.backend.repository.UserRepository;
import com.pipeline.backend.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    private final SecurityUtils securityUtils;

    private final AuthRepository authRepository;

    private final MailService mailService;

    private final JwtService jwtService;

    private static final int OTP_EXPIRY_MINUTES = 10;

    @Transactional
    public SignupInitialResponseDTO initiateSignup(SignupInitialRequestDTO requestDTO) {
        log.info("Initiating signup for email: {}", requestDTO.getEmail());

        /** 1. Validate the email against db with the following conditions
         *  - If the User already exist and is verified -> throw error "Email already registered, please login."
         *  - If the User already exist and is unverified -> reuse the existing user and proceed with otp
         *  - If the User does not exist -> create a new unverified user with status as pending,
         *    isVerified as false and provider as local and proceed with otp
         * **/
        Optional<User> checkUser = userRepository.findByEmail(requestDTO.getEmail());
        User user = checkUser
                .map(existingUser -> {
                    if(existingUser.isVerified()) {
                        log.info("User Already Exists with email: {}", requestDTO.getEmail());
                        throw new AuthenticationException("Email already registered, please login.");
                    }
                    // C1. User Already exists but is unverified -> reuse the existing user
                    return existingUser;
                } )
                // C2. User does not exist -> create a new unverified user
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .fullName(requestDTO.getFullName())
                            .email(requestDTO.getEmail())
                            .verified(false)
                            .provider(Provider.LOCAL)
                            .status(Status.PENDING)
                            .build();

                        log.info("Creating new unverified user for email: {}", requestDTO.getEmail());
                        return userRepository.save(newUser);
                });
        //2. If user is found check if an otp is recently generated and is still valid, if yes then do not generate a new otp
        Optional<AuthCode> existingAuthCode = authRepository.findTopByUserIdOrderByExpiresAtDesc(user.getId());
        if(existingAuthCode.isPresent()
                && (null != existingAuthCode.get().getAuthCode() && existingAuthCode.get().getAuthCode().equals(CodeType.SIGNUP_OTP))
                && existingAuthCode.get().getExpiresAt().isAfter(Instant.now())) {
            log.info("An active OTP already exists for user ID: {}. Not generating a new OTP.", user.getId());
            throw new AuthenticationException("An OTP has already been sent to your email. Please check your inbox.");
        }


        // 2. Generate a verification token/otp, save it to the db with expiry time and send email to user
        String otp = securityUtils.generateOTP();
        String hashedOtp = securityUtils.hashOTP(otp);

        Instant expiryTime = Instant.now().plus(Duration.ofMinutes(OTP_EXPIRY_MINUTES));
        AuthCode authCode = AuthCode.builder()
                .userId(user.getId())
                .codeHash(hashedOtp)
                .authCode(CodeType.SIGNUP_OTP)
                .expiresAt(expiryTime)
                .build();

        authRepository.save(authCode);
        log.debug("AuthCode created for user ID: {} with expiry at: {}", user.getId(), LocalDateTime.ofInstant(expiryTime, java.time.ZoneId.systemDefault()));

        mailService.sendOTPEMail(requestDTO.getEmail(), requestDTO.getFullName(), otp, expiryTime);
        log.info("OTP email sent to: {}", requestDTO.getEmail());

        // 3. Now take the userId, encrypt it with AES, generate jwt with claims and send response back to user
        return SignupInitialResponseDTO.builder()
                .signupAuthToken(jwtService.generateSignupToken(securityUtils.saltAndEncryptID(user.getId().toString())))
                .build();
    }
}
