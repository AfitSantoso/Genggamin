package com.example.genggamin.controller;

import com.example.genggamin.dto.ApiResponse;
import com.example.genggamin.dto.CustomerRequest;
import com.example.genggamin.dto.CustomerResponse;
import com.example.genggamin.service.CustomerService;
import com.example.genggamin.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerService customerService;
  private final UserService userService;
  private final ObjectMapper objectMapper;

  @PostMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<CustomerResponse>> createOrUpdateProfile(
      @RequestPart("data") String requestJson,
      @RequestPart(value = "ktp", required = false) MultipartFile fileKtp,
      @RequestPart(value = "selfie", required = false) MultipartFile fileSelfie,
      @RequestPart(value = "payslip", required = false) MultipartFile filePayslip) throws JsonProcessingException {
    
    // Deserialize manually
    CustomerRequest request = objectMapper.readValue(requestJson, CustomerRequest.class);
    
    Long userId = getAuthenticatedUserId();

    CustomerResponse customerResponse =
        customerService.createOrUpdateCustomer(userId, request, fileKtp, fileSelfie, filePayslip);

    ApiResponse<CustomerResponse> response =
        new ApiResponse<>(true, "Customer profile saved successfully", customerResponse);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/profile")
  public ResponseEntity<ApiResponse<CustomerResponse>> getCurrentUserProfile() {
    Long userId = getAuthenticatedUserId();

    CustomerResponse customerResponse = customerService.getCustomerByUserId(userId);

    ApiResponse<CustomerResponse> response =
        new ApiResponse<>(true, "Customer profile retrieved successfully", customerResponse);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/has-profile")
  public ResponseEntity<ApiResponse<Boolean>> hasCustomerProfile() {
    Long userId = getAuthenticatedUserId();

    boolean hasProfile = customerService.hasCustomerData(userId);

    ApiResponse<Boolean> response =
        new ApiResponse<>(
            true,
            hasProfile ? "Customer profile exists" : "Customer profile not found",
            hasProfile);

    return ResponseEntity.ok(response);
  }

  @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<CustomerResponse>> updateProfile(
      @RequestPart("data") String requestJson,
      @RequestPart(value = "ktp", required = false) MultipartFile fileKtp,
      @RequestPart(value = "selfie", required = false) MultipartFile fileSelfie,
      @RequestPart(value = "payslip", required = false) MultipartFile filePayslip) throws JsonProcessingException {
    
    CustomerRequest request = objectMapper.readValue(requestJson, CustomerRequest.class);
    
    Long userId = getAuthenticatedUserId();

    CustomerResponse customerResponse =
        customerService.createOrUpdateCustomer(userId, request, fileKtp, fileSelfie, filePayslip);

    ApiResponse<CustomerResponse> response =
        new ApiResponse<>(true, "Customer profile updated successfully", customerResponse);

    return ResponseEntity.ok(response);
  }

  private Long getAuthenticatedUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return userService.getUserIdByUsername(username);
  }
}
