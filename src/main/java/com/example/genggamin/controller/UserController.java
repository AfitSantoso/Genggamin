package com.example.genggamin.controller;

import com.example.genggamin.dto.ApiResponse;
import com.example.genggamin.dto.CreateUserRequest;
import com.example.genggamin.dto.UpdateUserRequest;
import com.example.genggamin.dto.UserResponse;
import com.example.genggamin.entity.User;
import com.example.genggamin.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<java.util.List<UserResponse>>> getAllUsers() {
    // Service sekarang return DTO langsung, bukan Entity
    java.util.List<UserResponse> data = userService.getAllUsers();

    if (data.isEmpty()) {
      ApiResponse<java.util.List<UserResponse>> resp =
          new ApiResponse<>(true, "No users found", java.util.Collections.emptyList());
      return ResponseEntity.ok(resp);
    }

    ApiResponse<java.util.List<UserResponse>> resp =
        new ApiResponse<>(true, "Users retrieved successfully", data);
    return ResponseEntity.ok(resp);
  }

  @PostMapping
  @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
    try {
      User saved = userService.createUserFromRequest(req);
      return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "User created successfully", mapToUserResponse(saved)));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
    }
  }

  @PutMapping("/staff/{id}")
  @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<?> updateStaffUser(@PathVariable Long id, @RequestBody UpdateUserRequest req) {
    try {
      User updated = userService.updateStaffUser(id, req);
      return ResponseEntity.ok(new ApiResponse<>(true, "Staff user updated successfully", mapToUserResponse(updated)));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
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

  /**
   * Get user by ID - Hanya user yang sedang login yang bisa mengakses data mereka sendiri
   * Menggunakan Authentication untuk mendapatkan username dari JWT token
   *
   * @param userId ID user yang akan diambil
   * @param authentication Object Authentication dari Spring Security (otomatis diinjeksi dari JWT
   *     token)
   * @return ResponseEntity dengan data user jika berhasil, atau error jika tidak memiliki akses
   */
  @GetMapping("/{userId}")
  public ResponseEntity<ApiResponse<UserResponse>> getUserById(
      @PathVariable Long userId, Authentication authentication) {
    try {
      // Mendapatkan username dari JWT token yang sedang login
      String currentUsername = authentication.getName();

      // Service akan memvalidasi apakah user yang login berhak mengakses data ini
      UserResponse user = userService.getUserById(userId, currentUsername);

      ApiResponse<UserResponse> response =
          new ApiResponse<>(true, "User retrieved successfully", user);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      ApiResponse<UserResponse> response = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
  }

  // @PostMapping("/login")
  // public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
  //     LoginResponse res = userService.Login(req);
  //     return ResponseEntity.ok(res);
  // }

}
