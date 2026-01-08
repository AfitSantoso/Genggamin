package com.example.genggamin.controller;

import com.example.genggamin.dto.ApiResponse;
import com.example.genggamin.dto.LoanActionRequest;
import com.example.genggamin.dto.LoanRequest;
import com.example.genggamin.dto.LoanResponse;
import com.example.genggamin.dto.LoanWithApprovalResponse;
import com.example.genggamin.dto.LoanWithDisbursementResponse;
import com.example.genggamin.dto.LoanWithReviewResponse;
import com.example.genggamin.service.LoanService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans")
public class LoanController {

  private final LoanService loanService;

  public LoanController(LoanService loanService) {
    this.loanService = loanService;
  }

  /** CUSTOMER: Submit a loan application */
  @PostMapping("/submit")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<ApiResponse<LoanResponse>> submitLoan(
      @RequestBody LoanRequest request, Authentication authentication) {
    try {
      String username = authentication.getName();
      LoanResponse response = loanService.submitLoan(username, request);
      return ResponseEntity.ok(
          ApiResponse.<LoanResponse>builder()
              .success(true)
              .message("Loan submitted successfully")
              .data(response)
              .build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.<LoanResponse>builder().success(false).message(e.getMessage()).build());
    }
  }

  /** CUSTOMER: Get my loan applications */
  @GetMapping("/my-loans")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<ApiResponse<List<LoanResponse>>> getMyLoans(Authentication authentication) {
    try {
      String username = authentication.getName();
      List<LoanResponse> loans = loanService.getMyLoans(username);
      return ResponseEntity.ok(
          ApiResponse.<List<LoanResponse>>builder()
              .success(true)
              .message("Loans retrieved successfully")
              .data(loans)
              .build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.<List<LoanResponse>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .build());
    }
  }

  /** MARKETING: Review submitted loans */
  @GetMapping("/review")
  @PreAuthorize("hasAnyRole('MARKETING', 'ADMIN')")
  public ResponseEntity<ApiResponse<List<LoanResponse>>> getLoansForReview() {
    try {
      List<LoanResponse> loans = loanService.getLoansForReview();
      return ResponseEntity.ok(
          ApiResponse.<List<LoanResponse>>builder()
              .success(true)
              .message("Loans retrieved successfully")
              .data(loans)
              .build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.<List<LoanResponse>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .build());
    }
  }

  /** MARKETING: Review a loan */
  @PostMapping("/review/{loanId}")
  @PreAuthorize("hasAnyRole('MARKETING', 'ADMIN')")
  public ResponseEntity<ApiResponse<LoanResponse>> reviewLoan(
      @PathVariable Long loanId,
      @RequestBody LoanActionRequest request,
      Authentication authentication) {
    try {
      String username = authentication.getName();
      LoanResponse response = loanService.reviewLoan(loanId, username, request);
      return ResponseEntity.ok(
          ApiResponse.<LoanResponse>builder()
              .success(true)
              .message("Loan reviewed successfully")
              .data(response)
              .build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.<LoanResponse>builder().success(false).message(e.getMessage()).build());
    }
  }

  /** MARKETING/ADMIN: Get all reviewed loans with review details */
  @GetMapping("/reviewed")
  @PreAuthorize("hasAnyRole('MARKETING', 'ADMIN')")
  public ResponseEntity<ApiResponse<List<LoanWithReviewResponse>>> getReviewedLoans() {
    try {
      List<LoanWithReviewResponse> loans = loanService.getReviewedLoans();
      return ResponseEntity.ok(
          ApiResponse.<List<LoanWithReviewResponse>>builder()
              .success(true)
              .message("Reviewed loans retrieved successfully")
              .data(loans)
              .build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.<List<LoanWithReviewResponse>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .build());
    }
  }

  /** BRANCH_MANAGER: Get loans for approval */
  @GetMapping("/approve")
  @PreAuthorize("hasAnyRole('BRANCH_MANAGER', 'ADMIN')")
  public ResponseEntity<ApiResponse<List<LoanResponse>>> getLoansForApproval() {
    try {
      List<LoanResponse> loans = loanService.getLoansForApproval();
      return ResponseEntity.ok(
          ApiResponse.<List<LoanResponse>>builder()
              .success(true)
              .message("Loans retrieved successfully")
              .data(loans)
              .build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.<List<LoanResponse>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .build());
    }
  }

  /** BRANCH_MANAGER: Approve or reject a loan */
  @PostMapping("/approve/{loanId}")
  @PreAuthorize("hasAnyRole('BRANCH_MANAGER', 'ADMIN')")
  public ResponseEntity<ApiResponse<LoanResponse>> approveLoan(
      @PathVariable Long loanId,
      @RequestBody LoanActionRequest request,
      Authentication authentication) {
    try {
      String username = authentication.getName();
      LoanResponse response = loanService.approveLoan(loanId, username, request);
      return ResponseEntity.ok(
          ApiResponse.<LoanResponse>builder()
              .success(true)
              .message("Loan processed successfully")
              .data(response)
              .build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.<LoanResponse>builder().success(false).message(e.getMessage()).build());
    }
  }

  /** BRANCH_MANAGER/ADMIN: Get all approved/rejected loans with approval details */
  @GetMapping("/approved")
  @PreAuthorize("hasAnyRole('BRANCH_MANAGER', 'ADMIN')")
  public ResponseEntity<ApiResponse<List<LoanWithApprovalResponse>>> getApprovedLoans() {
    try {
      List<LoanWithApprovalResponse> loans = loanService.getApprovedLoansWithDetails();
      return ResponseEntity.ok(
          ApiResponse.<List<LoanWithApprovalResponse>>builder()
              .success(true)
              .message("Approved loans retrieved successfully")
              .data(loans)
              .build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.<List<LoanWithApprovalResponse>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .build());
    }
  }

  /** BACK_OFFICE: Get loans for disbursement */
  @GetMapping("/disburse")
  @PreAuthorize("hasAnyRole('BACK_OFFICE', 'ADMIN')")
  public ResponseEntity<ApiResponse<List<LoanResponse>>> getLoansForDisbursement() {
    try {
      List<LoanResponse> loans = loanService.getLoansForDisbursement();
      return ResponseEntity.ok(
          ApiResponse.<List<LoanResponse>>builder()
              .success(true)
              .message("Loans retrieved successfully")
              .data(loans)
              .build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.<List<LoanResponse>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .build());
    }
  }

  /** BACK_OFFICE/ADMIN: Get all disbursed loans with disbursement details */
  @GetMapping("/disbursed")
  @PreAuthorize("hasAnyRole('BACK_OFFICE', 'ADMIN')")
  public ResponseEntity<ApiResponse<List<LoanWithDisbursementResponse>>> getDisbursedLoans() {
    try {
      List<LoanWithDisbursementResponse> loans = loanService.getDisbursedLoansWithDetails();
      return ResponseEntity.ok(
          ApiResponse.<List<LoanWithDisbursementResponse>>builder()
              .success(true)
              .message("Disbursed loans retrieved successfully")
              .data(loans)
              .build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.<List<LoanWithDisbursementResponse>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .build());
    }
  }

  /** BACK_OFFICE: Disburse a loan */
  @PostMapping("/disburse/{loanId}")
  @PreAuthorize("hasAnyRole('BACK_OFFICE', 'ADMIN')")
  public ResponseEntity<ApiResponse<LoanResponse>> disburseLoan(
      @PathVariable Long loanId,
      @RequestBody LoanActionRequest request,
      Authentication authentication) {
    try {
      String username = authentication.getName();
      LoanResponse response = loanService.disburseLoan(loanId, username, request);
      return ResponseEntity.ok(
          ApiResponse.<LoanResponse>builder()
              .success(true)
              .message("Loan disbursed successfully")
              .data(response)
              .build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.<LoanResponse>builder().success(false).message(e.getMessage()).build());
    }
  }

  /** ADMIN: Get all loans */
  @GetMapping("/all")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<LoanResponse>>> getAllLoans() {
    try {
      List<LoanResponse> loans = loanService.getAllLoans();
      return ResponseEntity.ok(
          ApiResponse.<List<LoanResponse>>builder()
              .success(true)
              .message("All loans retrieved successfully")
              .data(loans)
              .build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.<List<LoanResponse>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .build());
    }
  }

  /** Get loan by ID (accessible by all authenticated users) */
  @GetMapping("/{loanId}")
  public ResponseEntity<ApiResponse<LoanResponse>> getLoanById(@PathVariable Long loanId) {
    try {
      LoanResponse loan = loanService.getLoanById(loanId);
      return ResponseEntity.ok(
          ApiResponse.<LoanResponse>builder()
              .success(true)
              .message("Loan retrieved successfully")
              .data(loan)
              .build());
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.<LoanResponse>builder().success(false).message(e.getMessage()).build());
    }
  }
}
