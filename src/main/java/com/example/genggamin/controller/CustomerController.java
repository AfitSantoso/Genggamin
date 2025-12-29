package com.example.genggamin.controller;

import com.example.genggamin.dto.ApiResponse;
import com.example.genggamin.dto.CustomerRequest;
import com.example.genggamin.dto.CustomerResponse;
import com.example.genggamin.service.CustomerService;
import com.example.genggamin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final UserService userService;

    /**
     * Endpoint untuk create/update data customer oleh user yang sedang login
     * POST /customers/profile
     */
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<CustomerResponse>> createOrUpdateProfile(
            @RequestBody CustomerRequest request) {
        try {
            // Get current logged-in user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Extract user ID from authentication (assuming username is stored in authentication)
            // You might need to adjust this based on your JWT implementation
            Long userId = extractUserIdFromAuthentication(authentication);
            
            CustomerResponse customerResponse = customerService.createOrUpdateCustomer(userId, request);
            
            ApiResponse<CustomerResponse> response = new ApiResponse<>(
                true, 
                "Customer profile saved successfully", 
                customerResponse
            );
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            ApiResponse<CustomerResponse> errorResponse = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Endpoint untuk mendapatkan data customer user yang sedang login
     * GET /customers/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCurrentUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = extractUserIdFromAuthentication(authentication);
            
            CustomerResponse customerResponse = customerService.getCustomerByUserId(userId);
            
            ApiResponse<CustomerResponse> response = new ApiResponse<>(
                true, 
                "Customer profile retrieved successfully", 
                customerResponse
            );
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            ApiResponse<CustomerResponse> errorResponse = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Endpoint untuk cek apakah user sudah memiliki data customer
     * GET /customers/has-profile
     */
    @GetMapping("/has-profile")
    public ResponseEntity<ApiResponse<Boolean>> hasCustomerProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = extractUserIdFromAuthentication(authentication);
            
            boolean hasProfile = customerService.hasCustomerData(userId);
            
            ApiResponse<Boolean> response = new ApiResponse<>(
                true, 
                hasProfile ? "Customer profile exists" : "Customer profile not found", 
                hasProfile
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ApiResponse<Boolean> errorResponse = new ApiResponse<>(
                false, 
                e.getMessage(), 
                false
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Helper method untuk extract user ID dari Authentication object
     * Menggunakan UserService untuk mendapatkan user ID dari username
     */
    private Long extractUserIdFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userService.getUserIdByUsername(username);
    }
}
