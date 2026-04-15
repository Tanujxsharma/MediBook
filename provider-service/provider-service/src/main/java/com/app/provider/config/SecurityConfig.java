package com.app.provider.config;

import com.app.provider.security.JwtFilter;
import com.app.provider.security.JwtUtil;
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
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/providers/internal/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/providers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/providers/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/providers/specialization").permitAll()
                        .requestMatchers(HttpMethod.GET, "/providers/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/providers/add").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/providers/me").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.PUT, "/providers/me").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.PUT, "/providers/me/availability").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.PUT, "/providers/*/verify").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
