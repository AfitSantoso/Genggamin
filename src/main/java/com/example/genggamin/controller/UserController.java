package com.example.genggamin.controller;

import com.example.genggamin.dto.CreateUserRequest;
import com.example.genggamin.dto.ApiResponse;
import com.example.genggamin.dto.UserResponse;
import com.example.genggamin.entity.User;
import com.example.genggamin.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            ApiResponse<java.util.List<UserResponse>> resp = new ApiResponse<>(true, "No users found", java.util.Collections.emptyList());
            return ResponseEntity.ok(resp);
        }

        ApiResponse<java.util.List<UserResponse>> resp = new ApiResponse<>(true, "Users retrieved successfully", data);
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest req) {
        try {
            User saved = userService.createUserFromRequest(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // @PostMapping("/login")
    // public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
    //     LoginResponse res = userService.Login(req);
    //     return ResponseEntity.ok(res);
    // }

}
