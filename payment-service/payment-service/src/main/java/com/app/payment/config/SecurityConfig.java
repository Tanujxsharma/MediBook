package com.app.payment.config;

import com.app.payment.security.JwtFilter;
import com.app.payment.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR, jakarta.servlet.DispatcherType.FORWARD).permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/payments/webhook").permitAll()
                        .requestMatchers(HttpMethod.POST, "/payments/process").permitAll()
                        .requestMatchers(HttpMethod.POST, "/payments/create-order").permitAll()
                        .requestMatchers(HttpMethod.POST, "/payments/verify").permitAll()
                        .requestMatchers(HttpMethod.POST, "/payments/confirm").permitAll()
                        .requestMatchers(HttpMethod.GET, "/payments/my-payments").authenticated()
                        .requestMatchers(HttpMethod.GET, "/payments/provider/*").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.POST, "/payments/*/refund").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/payments/appointment/*/status").authenticated()
                        .requestMatchers(HttpMethod.GET, "/payments/appointment/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/payments/*").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
