// penambahan auth controller untuk handle login
package com.example.genggamin.controller;

import com.example.genggamin.dto.ApiResponse;
import com.example.genggamin.dto.CreateUserRequest;
import com.example.genggamin.dto.ForgotPasswordRequest;
import com.example.genggamin.dto.LoginRequest;
import com.example.genggamin.dto.LoginResponse;
import com.example.genggamin.dto.ResetPasswordRequest;
import com.example.genggamin.dto.UserResponse;
import com.example.genggamin.entity.User;
import com.example.genggamin.security.JwtUtil;
import com.example.genggamin.service.PasswordResetService;
import com.example.genggamin.service.TokenBlacklistService;
import com.example.genggamin.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final UserService userService;
  private final JwtUtil jwtUtil;
  private final TokenBlacklistService tokenBlacklistService;
  private final PasswordResetService passwordResetService;

  public AuthController(
      UserService userService,
      JwtUtil jwtUtil,
      TokenBlacklistService tokenBlacklistService,
      PasswordResetService passwordResetService) {
    this.userService = userService;
    this.jwtUtil = jwtUtil;
    this.tokenBlacklistService = tokenBlacklistService;
    this.passwordResetService = passwordResetService;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    try {
      User user = userService.authenticate(req);
      LoginResponse res = userService.Login(req);
      // collect roles as strings
      java.util.Set<String> roles =
          user.getRoles().stream()
              .map(r -> r.getName())
              .collect(java.util.stream.Collectors.toSet());
      String token = jwtUtil.generateToken(user.getUsername(), roles);
      res.setToken(token);
      return ResponseEntity.ok(res);
    } catch (RuntimeException e) {
      return ResponseEntity.status(401).body(java.util.Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody CreateUserRequest req) {
    try {
      User u = userService.register(req);
      return ResponseEntity.status(201)
          .body(java.util.Map.of("message", "User created", "id", u.getId()));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/register/staff")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<?> registerStaff(@RequestBody CreateUserRequest req) {
    try {
      User saved = userService.createStaffUser(req);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              new ApiResponse<>(true, "Staff user created successfully", mapToUserResponse(saved)));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(
      @RequestHeader(value = "Authorization", required = false) String authHeader) {
    try {
      // Extract token from Authorization header
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);

        // Get token expiration time
        long expirationTime = jwtUtil.getExpirationTimeFromToken(token);

        // Add token to blacklist
        tokenBlacklistService.blacklistToken(token, expirationTime);

        return ResponseEntity.ok(
            java.util.Map.of(
                "success", true, "message", "Logout successful. Token has been invalidated."));
      } else {
        return ResponseEntity.badRequest()
            .body(
                java.util.Map.of(
                    "success", false, "message", "Authorization header is missing or invalid"));
      }
    } catch (Exception e) {
      return ResponseEntity.status(500)
          .body(java.util.Map.of("success", false, "message", "Logout failed: " + e.getMessage()));
    }
  }

  /** Endpoint untuk forgot password User request reset password dengan email */
  @PostMapping("/forgot-password")
  public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
    try {
      String token = passwordResetService.processForgotPassword(request.getEmail());
      return ResponseEntity.ok(
          java.util.Map.of(
              "success",
              true,
              "message",
              "Link reset password telah dikirim ke email Anda. Silakan cek inbox atau spam folder.",
              "token",
              token));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
          .body(java.util.Map.of("success", false, "message", e.getMessage()));
    }
  }

  /**
   * Endpoint untuk reset password dengan token User menggunakan token dari email untuk set password
   * baru
   */
  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
    try {
      passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
      return ResponseEntity.ok(
          java.util.Map.of(
              "success",
              true,
              "message",
              "Password berhasil direset. Silakan login dengan password baru Anda."));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
          .body(java.util.Map.of("success", false, "message", e.getMessage()));
    }
  }

  private UserResponse mapToUserResponse(User user) {
    UserResponse dto = new UserResponse();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());
    dto.setFullName(user.getFullName());
    dto.setIsActive(user.getIsActive());
    if (user.getRoles() != null) {
      dto.setRoles(
          user.getRoles().stream()
              .map(role -> role.getName())
              .collect(java.util.stream.Collectors.toList()));
    }
    return dto;
  }
}
