// penambahan auth controller untuk handle login
package com.example.genggamin.controller;
import com.example.genggamin.dto.CreateUserRequest;
import com.example.genggamin.dto.LoginRequest;
import com.example.genggamin.dto.LoginResponse;
import com.example.genggamin.security.JwtUtil;
import com.example.genggamin.service.TokenBlacklistService;
import com.example.genggamin.service.UserService;
import com.example.genggamin.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(UserService userService, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            User user = userService.authenticate(req);
            LoginResponse res = userService.Login(req);
            // collect roles as strings
            java.util.Set<String> roles = user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
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
            return ResponseEntity.status(201).body(java.util.Map.of("message", "User created", "id", u.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token from Authorization header
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                // Get token expiration time
                long expirationTime = jwtUtil.getExpirationTimeFromToken(token);
                
                // Add token to blacklist
                tokenBlacklistService.blacklistToken(token, expirationTime);
                
                return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "message", "Logout successful. Token has been invalidated."
                ));
            } else {
                return ResponseEntity.badRequest().body(java.util.Map.of(
                    "success", false,
                    "message", "Authorization header is missing or invalid"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of(
                "success", false,
                "message", "Logout failed: " + e.getMessage()
            ));
        }
    }
}