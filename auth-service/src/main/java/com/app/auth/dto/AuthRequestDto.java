package com.app.auth.dto;

import lombok.Data;

@Data
public class AuthRequestDto {
   private String email;
   private String password;
}
