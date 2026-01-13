package com.example.genggamin.controller;

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
  public ResponseEntity<Map<String, Object>> root() {
    Map<String, Object> response = new HashMap<>();
    response.put("application", appName);
    response.put("status", "running");
    response.put("timestamp", LocalDateTime.now());
    response.put("message", "Welcome to Genggamin Loan Management API");
    response.put("documentation", "/swagger-ui.html");
    return ResponseEntity.ok(response);
  }

  @GetMapping({"/health", "/api/health"})
  public ResponseEntity<Map<String, String>> health() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "UP");
    response.put("timestamp", LocalDateTime.now().toString());
    return ResponseEntity.ok(response);
  }
}
