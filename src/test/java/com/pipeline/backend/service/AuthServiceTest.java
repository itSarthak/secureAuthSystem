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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private MailService mailService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private SignupInitialRequestDTO requestDTO;
    private UUID userId1;
    private UUID userId2;

    @BeforeEach
    void setUp() {
        requestDTO = SignupInitialRequestDTO.builder()
                .email("test@example.com")
                .fullName("Test User")
                .build();
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
    }

    @Test
    void testInitiateSignup_UserExistsAndVerified_ShouldThrowException() {
        // Arrange
        User existingUser = User.builder()
                .id(userId1)
                .email("test@example.com")
                .verified(true)
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.initiateSignup(requestDTO);
        });
        assertEquals("Email already registered, please login.", exception.getMessage());
    }

    @Test
    void testInitiateSignup_UserExistsButUnverified_ShouldReuseUser() {
        // Arrange
        User existingUser = User.builder()
                .id(userId1)
                .email("test@example.com")
                .verified(false)
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(authRepository.findTopByUserIdOrderByExpiresAtDesc(userId1)).thenReturn(Optional.empty());
        when(securityUtils.generateOTP()).thenReturn("123456");
        when(securityUtils.hashOTP("123456")).thenReturn("hashedOtp");
        when(securityUtils.saltAndEncryptID(userId1.toString())).thenReturn("encryptedId");
        when(jwtService.generateSignupToken("encryptedId")).thenReturn("jwtToken");

        // Act
        SignupInitialResponseDTO response = authService.initiateSignup(requestDTO);

        // Assert
        assertNotNull(response);
        assertEquals("jwtToken", response.getSignupAuthToken());
        verify(userRepository, never()).save(any(User.class));
        verify(authRepository).save(any(AuthCode.class));
        verify(mailService).sendOTPEMail(eq("test@example.com"), eq("Test User"), eq("123456"), any(Instant.class));
    }

    @Test
    void testInitiateSignup_UserDoesNotExist_ShouldCreateNewUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        User newUser = User.builder()
                .id(userId2)
                .email("test@example.com")
                .fullName("Test User")
                .verified(false)
                .provider(Provider.LOCAL)
                .status(Status.PENDING)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(authRepository.findTopByUserIdOrderByExpiresAtDesc(userId2)).thenReturn(Optional.empty());
        when(securityUtils.generateOTP()).thenReturn("123456");
        when(securityUtils.hashOTP("123456")).thenReturn("hashedOtp");
        when(securityUtils.saltAndEncryptID(userId2.toString())).thenReturn("encryptedId");
        when(jwtService.generateSignupToken("encryptedId")).thenReturn("jwtToken");

        // Act
        SignupInitialResponseDTO response = authService.initiateSignup(requestDTO);

        // Assert
        assertNotNull(response);
        assertEquals("jwtToken", response.getSignupAuthToken());
        verify(userRepository).save(any(User.class));
        verify(authRepository).save(any(AuthCode.class));
        verify(mailService).sendOTPEMail(eq("test@example.com"), eq("Test User"), eq("123456"), any(Instant.class));
    }

    @Test
    void testInitiateSignup_ActiveOTPExists_ShouldThrowException() {
        // Arrange
        User existingUser = User.builder()
                .id(userId1)
                .email("test@example.com")
                .verified(false)
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        AuthCode activeAuthCode = AuthCode.builder()
                .userId(userId1)
                .authCode(CodeType.SIGNUP_OTP)
                .expiresAt(Instant.now().plusSeconds(600)) // future
                .build();
        when(authRepository.findTopByUserIdOrderByExpiresAtDesc(userId1)).thenReturn(Optional.of(activeAuthCode));

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.initiateSignup(requestDTO);
        });
        assertEquals("An OTP has already been sent to your email. Please check your inbox.", exception.getMessage());
    }

    @Test
    void testInitiateSignup_NoActiveOTP_ShouldGenerateNewOTP() {
        // Arrange
        User existingUser = User.builder()
                .id(userId1)
                .email("test@example.com")
                .verified(false)
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        AuthCode expiredAuthCode = AuthCode.builder()
                .userId(userId1)
                .authCode(CodeType.SIGNUP_OTP)
                .expiresAt(Instant.now().minusSeconds(600)) // past
                .build();
        when(authRepository.findTopByUserIdOrderByExpiresAtDesc(userId1)).thenReturn(Optional.of(expiredAuthCode));
        when(securityUtils.generateOTP()).thenReturn("123456");
        when(securityUtils.hashOTP("123456")).thenReturn("hashedOtp");
        when(securityUtils.saltAndEncryptID(userId1.toString())).thenReturn("encryptedId");
        when(jwtService.generateSignupToken("encryptedId")).thenReturn("jwtToken");

        // Act
        SignupInitialResponseDTO response = authService.initiateSignup(requestDTO);

        // Assert
        assertNotNull(response);
        assertEquals("jwtToken", response.getSignupAuthToken());
        verify(authRepository).save(any(AuthCode.class));
        verify(mailService).sendOTPEMail(eq("test@example.com"), eq("Test User"), eq("123456"), any(Instant.class));
    }
}