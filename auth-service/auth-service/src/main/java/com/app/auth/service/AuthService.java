package com.app.auth.service;

import com.app.auth.dto.AuthResponseDto;
import com.app.auth.entity.PasswordResetToken;
import com.app.auth.entity.User;
import com.app.auth.repository.PasswordResetTokenRepository;
import com.app.auth.repository.UserRepository;
import com.app.auth.security.JwtUtil;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository tokenRepository,
                       BCryptPasswordEncoder encoder,
                       JwtUtil jwtUtil,
                       NotificationService notificationService,
                       RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
        this.notificationService = notificationService;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public String signup(String fullname,
                         String email,
                         String password,
                         String role,
                         String specialization,
                         String clinicName) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists with email: " + email);
        }

        String userRole = (role == null || role.isBlank()) ? "PATIENT" : role.trim().toUpperCase();

        if (!userRole.equals("PATIENT") && !userRole.equals("DOCTOR") && !userRole.equals("ADMIN")) {
            throw new RuntimeException("Invalid role");
        }

        User user = User.builder()
                .fullname(fullname)
                .email(email)
                .password(encoder.encode(password))
                .role(userRole)
                .provider("LOCAL")
                .build();

        user = userRepository.save(user);

        if ("DOCTOR".equals(userRole)) {
            try {
                String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(token);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("userId", user.getId());
                requestBody.put("name", user.getFullname());
                requestBody.put("specialization", specialization == null || specialization.isBlank() ? "General" : specialization);
                requestBody.put("qualification", "");
                requestBody.put("experienceYears", 0);
                requestBody.put("bio", "");
                requestBody.put("clinicName", clinicName == null || clinicName.isBlank() ? "Default Clinic" : clinicName);
                requestBody.put("clinicAddress", "");

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                restTemplate.postForObject("http://localhost:8082/providers/add", request, String.class);
            } catch (Exception ex) {
                // If the external call fails, we throw an exception which will roll back the transaction
                throw new RuntimeException("Doctor profile creation failed: " + ex.getMessage());
            }
        }

        return "User registered successfully";
    }

    public AuthResponseDto login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        return AuthResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .message("Login successful")
                .build();
    }

    @Transactional
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryTime(LocalDateTime.now().plusMinutes(30))
                .build();

        tokenRepository.save(resetToken);
        notificationService.sendPasswordResetNotification(user.getEmail(), token);

        return "Password reset token generated and notification sent";
    }

    @Transactional
    public String resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (resetToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);

        return "Password reset successful";
    }
}
