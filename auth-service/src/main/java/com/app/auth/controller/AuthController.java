package com.app.auth.controller;

import com.app.auth.dto.AuthRequestDto;
import com.app.auth.dto.AuthResponseDto;
import com.app.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/signup")
    public String  signup(@RequestBody AuthRequestDto req) {
        return service.signup(req.getEmail(),req.getPassword());
    }

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody AuthRequestDto req) {
        String token =service.login(req.getEmail(), req.getPassword());
        return new AuthResponseDto(token);
    }


}