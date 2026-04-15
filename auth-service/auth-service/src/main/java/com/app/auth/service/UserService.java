package com.app.auth.service;

import com.app.auth.dto.AdminUserDto;
import com.app.auth.dto.UserProfileDto;
import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileDto getUserProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDto dto = new UserProfileDto();
        dto.setName(user.getFullname());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public UserProfileDto updateUserProfile(Long userId, UserProfileDto profileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullname(profileDto.getName());
        user.setEmail(profileDto.getEmail());

        userRepository.save(user);

        UserProfileDto response = new UserProfileDto();
        response.setName(user.getFullname());
        response.setEmail(user.getEmail());
        return response;
    }

    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToAdminDto)
                .toList();
    }

    public List<AdminUserDto> getUsersByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() != null && user.getRole().equalsIgnoreCase(role))
                .map(this::mapToAdminDto)
                .toList();
    }

    private AdminUserDto mapToAdminDto(User user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .name(user.getFullname())
                .email(user.getEmail())
                .role(user.getRole())
                .provider(user.getProvider())
                .build();
    }
}
