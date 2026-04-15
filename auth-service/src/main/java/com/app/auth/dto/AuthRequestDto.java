package com.app.auth.dto;

import lombok.Data;

@Data
public class AuthRequestDto {
    private String fullname;
    private String email;
    private String password;
    
    // Optional fields for registration
    private String role; // "DOCTOR" or "PATIENT"
    private String specialization; // required if role is DOCTOR
    private String clinicName; // required if role is DOCTOR
}
