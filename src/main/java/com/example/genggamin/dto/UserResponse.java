package com.example.genggamin.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User DTO untuk response API dan Redis cache Menggunakan List<String> untuk roles agar mudah
 * di-serialize
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
  private Long id;
  private String username;
  private String email;
  private String fullName;
  private Boolean isActive;
  private List<String> roles; // Changed from Set<RoleResponse> to List<String> for better caching
}
