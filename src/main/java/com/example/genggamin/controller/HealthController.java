package com.example.genggamin.controller;

import com.example.genggamin.dto.ApiResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @Value("${spring.application.name:Genggamin API}")
  private String appName;

  @GetMapping("/")
  public ResponseEntity<ApiResponse<Map<String, Object>>> root() {
    Map<String, Object> response = new HashMap<>();
    response.put("application", appName);
    response.put("status", "running");
    response.put("timestamp", LocalDateTime.now());
    response.put("message", "Welcome to Genggamin Loan Management API");
    response.put("documentation", "/swagger-ui.html");
    return ResponseEntity.ok(
        new ApiResponse<>(true, "Application info retrieved successfully", response));
  }

  @GetMapping({"/health", "/api/health"})
  public ResponseEntity<ApiResponse<Map<String, String>>> health() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "UP");
    response.put("timestamp", LocalDateTime.now().toString());
    return ResponseEntity.ok(new ApiResponse<>(true, "Application is healthy", response));
  }
}
