package com.app.auth.config;

import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import com.app.auth.security.JwtFilter;
import com.app.auth.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final String FRONTEND_URL = "http://localhost:3000";

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtUtil jwtUtil,
            UserRepository userRepository
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login",
                                "/auth/signup",
                                "/oauth2/**",
                                "/login/**",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth.successHandler(
                        (req, res, authentication) -> {
                            String email = authentication.getName();

                            if (userRepository.findByEmail(email).isEmpty()) {
                                User user = User.builder()
                                        .email(email)
                                        .password("")
                                        .role("PATIENT")
                                        .provider("GOOGLE")
                                        .fullname("OAuth User")
                                        .build();
                                userRepository.save(user);
                            }

                            User user = userRepository.findByEmail(email).get();
                            String token = jwtUtil.generateToken(email, user.getRole());
                            res.sendRedirect(FRONTEND_URL + "/oauth-success?token=" + token);
                        }
                ))
                .addFilterBefore(
                        new JwtFilter(jwtUtil),
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
