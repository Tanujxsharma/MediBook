package com.app.auth.service;

import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.app.auth.security.JwtUtil;

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


    public String signup(String email, String password) {

        if (repo.findByEmail(email).isPresent()) {
            return "User already exists";
        }

        User user = User.builder()
                .email(email)
                .password(encoder.encode(password))
                .role("PATIENT")
                .provider("LOCAL")
                .fullname("Default User")
                .build();

        repo.save(user);

        return "User registered";
    }

    public String login(String email, String password) {

        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(password, user.getPassword())) {
            return "Invalid password";
        }

        return jwtUtil.generateToken(email);
    }
}