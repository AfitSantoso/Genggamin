package com.example.genggamin.controller;

import com.example.genggamin.dto.ApiResponse;
import com.example.genggamin.dto.PlafondRequest;
import com.example.genggamin.dto.PlafondResponse;
import com.example.genggamin.service.PlafondService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller untuk mengelola Plafond
 * CRUD operations hanya bisa diakses oleh ADMIN
 * View operations bisa diakses oleh semua authenticated users
 */
@RestController
@RequestMapping("/plafonds")
public class PlafondController {

    private final PlafondService plafondService;

    public PlafondController(PlafondService plafondService) {
        this.plafondService = plafondService;
    }

    /**
     * Get all plafonds
     * PUBLIC - Accessible without authentication
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlafondResponse>>> getAllPlafonds() {
        try {
            List<PlafondResponse> plafonds = plafondService.getAllPlafonds();
            
            if (plafonds.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.<List<PlafondResponse>>builder()
                        .success(true)
                        .message("No plafonds found")
                        .data(plafonds)
                        .build());
            }
            
            return ResponseEntity.ok(ApiResponse.<List<PlafondResponse>>builder()
                    .success(true)
                    .message("Plafonds retrieved successfully")
                    .data(plafonds)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<PlafondResponse>>builder()
                            .success(false)
                            .message("Failed to retrieve plafonds: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get only active plafonds (isActive = true)
     * PUBLIC - Accessible without authentication
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PlafondResponse>>> getActivePlafonds() {
        try {
            List<PlafondResponse> plafonds = plafondService.getActivePlafonds();
            return ResponseEntity.ok(ApiResponse.<List<PlafondResponse>>builder()
                    .success(true)
                    .message("Active plafonds retrieved successfully")
                    .data(plafonds)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<PlafondResponse>>builder()
                            .success(false)
                            .message("Failed to retrieve active plafonds: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get plafond by ID
     * PUBLIC - Accessible without authentication
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlafondResponse>> getPlafondById(@PathVariable Long id) {
        try {
            PlafondResponse plafond = plafondService.getPlafondById(id);
            return ResponseEntity.ok(ApiResponse.<PlafondResponse>builder()
                    .success(true)
                    .message("Plafond retrieved successfully")
                    .data(plafond)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<PlafondResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<PlafondResponse>builder()
                            .success(false)
                            .message("Failed to retrieve plafond: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get plafonds by income range
     * PUBLIC - Accessible without authentication for checking eligibility
     */
    @GetMapping("/by-income/{income}")
    public ResponseEntity<ApiResponse<List<PlafondResponse>>> getPlafondsByIncome(
            @PathVariable BigDecimal income) {
        try {
            List<PlafondResponse> plafonds = plafondService.getPlafondsByIncome(income);
            return ResponseEntity.ok(ApiResponse.<List<PlafondResponse>>builder()
                    .success(true)
                    .message("Plafonds for income retrieved successfully")
                    .data(plafonds)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<PlafondResponse>>builder()
                            .success(false)
                            .message("Failed to retrieve plafonds: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Create new plafond
     * Only accessible by ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlafondResponse>> createPlafond(
            @RequestBody PlafondRequest request) {
        try {
            PlafondResponse plafond = plafondService.createPlafond(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<PlafondResponse>builder()
                            .success(true)
                            .message("Plafond created successfully")
                            .data(plafond)
                            .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<PlafondResponse>builder()
                            .success(false)
                            .message("Validation error: " + e.getMessage())
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<PlafondResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<PlafondResponse>builder()
                            .success(false)
                            .message("Failed to create plafond: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Update existing plafond
     * Only accessible by ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlafondResponse>> updatePlafond(
            @PathVariable Long id,
            @RequestBody PlafondRequest request) {
        try {
            PlafondResponse plafond = plafondService.updatePlafond(id, request);
            return ResponseEntity.ok(ApiResponse.<PlafondResponse>builder()
                    .success(true)
                    .message("Plafond updated successfully")
                    .data(plafond)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<PlafondResponse>builder()
                            .success(false)
                            .message("Validation error: " + e.getMessage())
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<PlafondResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<PlafondResponse>builder()
                            .success(false)
                            .message("Failed to update plafond: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Soft delete plafond
     * Only accessible by ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePlafond(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String deletedBy = authentication.getName();
            plafondService.deletePlafond(id, deletedBy);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Plafond deleted successfully")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Failed to delete plafond: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Toggle plafond active status
     * Only accessible by ADMIN
     */
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlafondResponse>> toggleActiveStatus(@PathVariable Long id) {
        try {
            PlafondResponse plafond = plafondService.toggleActiveStatus(id);
            return ResponseEntity.ok(ApiResponse.<PlafondResponse>builder()
                    .success(true)
                    .message("Plafond status toggled successfully")
                    .data(plafond)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<PlafondResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<PlafondResponse>builder()
                            .success(false)
                            .message("Failed to toggle plafond status: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Restore soft deleted plafond
     * Only accessible by ADMIN
     */
    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlafondResponse>> restorePlafond(@PathVariable Long id) {
        try {
            PlafondResponse plafond = plafondService.restorePlafond(id);
            return ResponseEntity.ok(ApiResponse.<PlafondResponse>builder()
                    .success(true)
                    .message("Plafond restored successfully")
                    .data(plafond)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<PlafondResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<PlafondResponse>builder()
                            .success(false)
                            .message("Failed to restore plafond: " + e.getMessage())
                            .build());
        }
    }
}
