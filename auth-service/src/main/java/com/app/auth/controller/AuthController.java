package com.app.auth.controller;

import com.app.auth.dto.AuthRequestDto;
import com.app.auth.dto.AuthResponseDto;
import com.app.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AuthRequestDto req) {
        String msg = service.signup(
            req.getFullname(), 
            req.getEmail(), 
            req.getPassword(),
            req.getRole(),
            req.getSpecialization(),
            req.getClinicName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(msg);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody AuthRequestDto req) {
        String token = service.login(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(new AuthResponseDto(token));
    }
}
