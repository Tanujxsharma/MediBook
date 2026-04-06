package com.app.appointment.config;

import com.app.appointment.security.JwtFilter;
import com.app.appointment.security.JwtUtil;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/slots/provider/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/slots").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.POST, "/appointments/book/**").hasRole("PATIENT")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
