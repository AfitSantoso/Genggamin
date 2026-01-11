package com.example.genggamin.controller;

import com.example.genggamin.dto.ApiResponse;
import com.example.genggamin.dto.CustomerRequest;
import com.example.genggamin.dto.CustomerResponse;
import com.example.genggamin.service.CustomerService;
import com.example.genggamin.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerService customerService;
  private final UserService userService;
  private final ObjectMapper objectMapper;

  /**
   * Endpoint untuk create/update data customer oleh user yang sedang login POST /customers/profile
   * Support Upload Foto KTP dan Selfie.
   */
  @PostMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<CustomerResponse>> createOrUpdateProfile(
      @RequestPart("data") String requestJson,
      @RequestPart(value = "ktp", required = false) MultipartFile fileKtp,
      @RequestPart(value = "selfie", required = false) MultipartFile fileSelfie,
      @RequestPart(value = "payslip", required = false) MultipartFile filePayslip) {
    try {
      // Deserialize string JSON manual untuk menghindari masalah Content-Type
      CustomerRequest request = objectMapper.readValue(requestJson, CustomerRequest.class);
      
      // Get current logged-in user
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String username = authentication.getName();

      // Extract user ID from authentication (assuming username is stored in authentication)
      // You might need to adjust this based on your JWT implementation
      Long userId = extractUserIdFromAuthentication(authentication);

      CustomerResponse customerResponse =
          customerService.createOrUpdateCustomer(userId, request, fileKtp, fileSelfie, filePayslip);

      ApiResponse<CustomerResponse> response =
          new ApiResponse<>(true, "Customer profile saved successfully", customerResponse);

      return ResponseEntity.ok(response);

    } catch (JsonProcessingException e) {
       ApiResponse<CustomerResponse> errorResponse = new ApiResponse<>(false, "Invalid JSON format in 'data' part: " + e.getMessage(), null);
       return ResponseEntity.badRequest().body(errorResponse);
    } catch (RuntimeException e) {
      ApiResponse<CustomerResponse> errorResponse = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  /** Endpoint untuk mendapatkan data customer user yang sedang login GET /customers/profile */
  @GetMapping("/profile")
  public ResponseEntity<ApiResponse<CustomerResponse>> getCurrentUserProfile() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      Long userId = extractUserIdFromAuthentication(authentication);

      CustomerResponse customerResponse = customerService.getCustomerByUserId(userId);

      ApiResponse<CustomerResponse> response =
          new ApiResponse<>(true, "Customer profile retrieved successfully", customerResponse);

      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      ApiResponse<CustomerResponse> errorResponse = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
  }

  /** Endpoint untuk cek apakah user sudah memiliki data customer GET /customers/has-profile */
  @GetMapping("/has-profile")
  public ResponseEntity<ApiResponse<Boolean>> hasCustomerProfile() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      Long userId = extractUserIdFromAuthentication(authentication);

      boolean hasProfile = customerService.hasCustomerData(userId);

      ApiResponse<Boolean> response =
          new ApiResponse<>(
              true,
              hasProfile ? "Customer profile exists" : "Customer profile not found",
              hasProfile);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      ApiResponse<Boolean> errorResponse = new ApiResponse<>(false, e.getMessage(), false);
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  /**
   * Endpoint untuk update data customer oleh user yang sedang login PUT /customers/profile Method
   * ini khusus untuk update, sudah support upload file document/image.
   */
  @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<CustomerResponse>> updateProfile(
      @RequestPart("data") String requestJson,
      @RequestPart(value = "ktp", required = false) MultipartFile fileKtp,
      @RequestPart(value = "selfie", required = false) MultipartFile fileSelfie,
      @RequestPart(value = "payslip", required = false) MultipartFile filePayslip) {
    try {
      // Deserialize string JSON
      CustomerRequest request = objectMapper.readValue(requestJson, CustomerRequest.class);

      // Get current logged-in user
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      Long userId = extractUserIdFromAuthentication(authentication);

      // Update customer profile
      CustomerResponse customerResponse =
          customerService.createOrUpdateCustomer(userId, request, fileKtp, fileSelfie, filePayslip);

      ApiResponse<CustomerResponse> response =
          new ApiResponse<>(true, "Customer profile updated successfully", customerResponse);

      return ResponseEntity.ok(response);

    } catch (JsonProcessingException e) {
      ApiResponse<CustomerResponse> errorResponse = new ApiResponse<>(false, "Invalid JSON format in 'data' part: " + e.getMessage(), null);
      return ResponseEntity.badRequest().body(errorResponse);
    } catch (RuntimeException e) {
      ApiResponse<CustomerResponse> errorResponse = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  /**
   * Helper method untuk extract user ID dari Authentication object Menggunakan UserService untuk
   * mendapatkan user ID dari username
   */
  private Long extractUserIdFromAuthentication(Authentication authentication) {
    String username = authentication.getName();
    return userService.getUserIdByUsername(username);
  }
}
