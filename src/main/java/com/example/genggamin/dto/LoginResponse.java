package com.example.genggamin.dto;

import lombok.Data;

@Data
public class LoginResponse {
  private Long id;
  private String username;
  private String email;
  private Boolean isActive;
  private String token;
  private Long expiresAt;
}
