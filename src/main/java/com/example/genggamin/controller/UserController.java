package com.example.genggamin.controller;

import com.example.genggamin.dto.CreateUserRequest;
import com.example.genggamin.dto.ApiResponse;
import com.example.genggamin.dto.UserResponse;
import com.example.genggamin.dto.RoleResponse;
import com.example.genggamin.entity.Role;
import com.example.genggamin.entity.User;
import com.example.genggamin.repository.RoleRepository;
import com.example.genggamin.service.UserService;
// import com.example.genggamin.dto.LoginRequest;
// import com.example.genggamin.dto.LoginResponse;

//import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<UserResponse>>> getAllUsers() {
        java.util.List<User> users = userService.getAllUsers();
        java.util.List<UserResponse> data = users.stream().map(u -> {
            java.util.Set<RoleResponse> roles = u.getRoles().stream().map(r -> new RoleResponse(r.getId(), r.getName(), r.getDescription())).collect(java.util.stream.Collectors.toSet());
            return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getPhone(), u.getIsActive(), roles);
        }).collect(java.util.stream.Collectors.toList());

        if (data.isEmpty()) {
            ApiResponse<java.util.List<UserResponse>> resp = new ApiResponse<>(true, "No users found", java.util.Collections.emptyList());
            return ResponseEntity.ok(resp);
        }

        ApiResponse<java.util.List<UserResponse>> resp = new ApiResponse<>(true, "Users retrieved successfully", data);
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest req) {
        Set<Role> roles = new HashSet<>();
        if (req.getRoles() != null) {
            for (String roleName : req.getRoles()) {
                Role r = roleRepository.findByName(roleName).orElse(null);
                if (r != null) roles.add(r);
            }
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(req.getPassword())
                .isActive(req.getIsActive())
                .roles(roles)
                .build();

        User saved = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // @PostMapping("/login")
    // public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
    //     LoginResponse res = userService.Login(req);
    //     return ResponseEntity.ok(res);
    // }

}
