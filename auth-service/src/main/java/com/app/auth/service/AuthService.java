package com.app.auth.service;

import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import com.app.auth.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository repo, BCryptPasswordEncoder encoder, JwtUtil jwtUtil) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public String signup(String fullname, String email, String password, String role, String specialization, String clinicName) {
        if (repo.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists with email: " + email);
        }

        String userRole = (role != null && !role.trim().isEmpty()) ? role.toUpperCase() : "PATIENT";

        User user = User.builder()
                .fullname(fullname)
                .email(email)
                .password(encoder.encode(password))
                .role(userRole)
                .provider("LOCAL")
                .build();

        user = repo.save(user);

        if ("DOCTOR".equals(userRole)) {
            try {
                String token = jwtUtil.generateToken(email, userRole);
                RestTemplate restTemplate = new RestTemplate();
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(token);

                Map<String, Object> providerRequest = new HashMap<>();
                providerRequest.put("userId", user.getId());
                providerRequest.put("name", fullname);
                providerRequest.put("specialization", specialization != null ? specialization : "General");
                providerRequest.put("clinicName", clinicName != null ? clinicName : "Default Clinic");

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(providerRequest, headers);
                restTemplate.postForObject("http://localhost:8082/providers/add", request, String.class);
            } catch (Exception e) {
                repo.deleteById(user.getId());
                throw new RuntimeException("Failed to register Doctor profile: " + e.getMessage());
            }
        }

        return "User registered successfully";
    }

    public String login(String email, String password) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(email, user.getRole());
    }
}
