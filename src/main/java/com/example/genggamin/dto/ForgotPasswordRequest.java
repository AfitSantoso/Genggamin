package com.example.genggamin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO untuk request forgot password
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {
    private String email;
}
