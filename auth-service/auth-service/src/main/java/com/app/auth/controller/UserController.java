package com.app.auth.controller;

import com.app.auth.dto.AdminUserDto;
import com.app.auth.dto.UserProfileDto;
import com.app.auth.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserProfileDto user = userService.getUserProfileById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateCurrentUser(
            Authentication authentication,
            @RequestBody UserProfileDto profileDto) {
        Long userId = (Long) authentication.getPrincipal();
        UserProfileDto updated = userService.updateUserProfile(userId, profileDto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/internal/{userId}")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable Long userId) {
        UserProfileDto user = userService.getUserProfileById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/admin/patients")
    public ResponseEntity<List<AdminUserDto>> getPatients() {
        return ResponseEntity.ok(userService.getUsersByRole("PATIENT"));
    }
}
