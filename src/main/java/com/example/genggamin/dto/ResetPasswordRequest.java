package com.example.genggamin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO untuk request reset password */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
  private String token;
  private String newPassword;
}
