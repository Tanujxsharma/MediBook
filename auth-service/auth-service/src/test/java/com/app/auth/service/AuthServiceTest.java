package com.app.auth.service;

import com.app.auth.dto.AuthResponseDto;
import com.app.auth.entity.PasswordResetToken;
import com.app.auth.entity.User;
import com.app.auth.repository.PasswordResetTokenRepository;
import com.app.auth.repository.UserRepository;
import com.app.auth.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_shouldRegisterPatientSuccessfully() {
        User savedUser = User.builder()
                .id(1L)
                .fullname("Test User")
                .email("test@example.com")
                .password("encoded-password")
                .role("PATIENT")
                .provider("LOCAL")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("secret")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        String response = authService.signup("Test User", "test@example.com", "secret", null, null, null);

        assertEquals("User registered successfully", response);
        verify(restTemplate, never()).postForObject(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void signup_shouldCreateDoctorProfileWhenRoleIsDoctor() {
        User savedUser = User.builder()
                .id(2L)
                .fullname("Dr Test")
                .email("doctor@example.com")
                .password("encoded-password")
                .role("DOCTOR")
                .provider("LOCAL")
                .build();

        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("secret")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(2L, "doctor@example.com", "DOCTOR")).thenReturn("jwt-token");
        when(restTemplate.postForObject(eq("http://localhost:8082/providers/add"), any(HttpEntity.class), eq(String.class)))
                .thenReturn("ok");

        String response = authService.signup("Dr Test", "doctor@example.com", "secret", "doctor", "Cardiology", "City Clinic");

        assertEquals("User registered successfully", response);
        verify(restTemplate).postForObject(eq("http://localhost:8082/providers/add"), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void login_shouldReturnAuthResponseForValidCredentials() {
        User user = User.builder()
                .id(3L)
                .fullname("Login User")
                .email("login@example.com")
                .password("encoded-password")
                .role("PATIENT")
                .provider("LOCAL")
                .build();

        when(userRepository.findByEmail("login@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("secret", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken(3L, "login@example.com", "PATIENT")).thenReturn("jwt-token");

        AuthResponseDto response = authService.login("login@example.com", "secret");

        assertEquals(3L, response.getUserId());
        assertEquals("jwt-token", response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void forgotPassword_shouldGenerateAndStoreResetToken() {
        User user = User.builder()
                .id(4L)
                .fullname("Forgot User")
                .email("forgot@example.com")
                .password("pass")
                .role("PATIENT")
                .provider("LOCAL")
                .build();

        when(userRepository.findByEmail("forgot@example.com")).thenReturn(Optional.of(user));

        String response = authService.forgotPassword("forgot@example.com");

        assertEquals("Password reset token generated and notification sent", response);
        verify(tokenRepository).deleteByUser(user);
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(notificationService).sendPasswordResetNotification(eq("forgot@example.com"), anyString());
    }

    @Test
    void resetPassword_shouldUpdatePasswordAndDeleteToken() {
        User user = User.builder()
                .id(5L)
                .fullname("Reset User")
                .email("reset@example.com")
                .password("old-password")
                .role("PATIENT")
                .provider("LOCAL")
                .build();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token("reset-token")
                .user(user)
                .expiryTime(LocalDateTime.now().plusMinutes(10))
                .build();

        when(tokenRepository.findByToken("reset-token")).thenReturn(Optional.of(resetToken));
        when(encoder.encode("new-password")).thenReturn("encoded-new-password");

        String response = authService.resetPassword("reset-token", "new-password");

        assertEquals("Password reset successful", response);
        assertEquals("encoded-new-password", user.getPassword());
        verify(userRepository).save(user);
        verify(tokenRepository).delete(resetToken);
    }
}
