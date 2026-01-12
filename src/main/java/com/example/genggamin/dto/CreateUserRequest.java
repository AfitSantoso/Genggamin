package com.example.genggamin.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
  private String username;
  private String email;
  private String fullName;
  private String password;
  private Boolean isActive;
  private Set<String> roles;
}
