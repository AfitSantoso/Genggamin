package com.example.genggamin.dto;

import com.example.genggamin.entity.Loan;
import com.example.genggamin.entity.LoanApproval;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWithApprovalResponse {
  // Loan data
  private Long id;
  private Long customerId;
  private Long plafondId;
  private BigDecimal loanAmount;
  private Long tenorMonth;
  private BigDecimal interestRate;
  private String purpose;
  private String status;
  private LocalDateTime submissionDate;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Approval data
  private Long approvalId;
  private Long approvedBy;
  private String approvalStatus;
  private String approvalNotes;
  private LocalDateTime approvedAt;

  public static LoanWithApprovalResponse fromEntities(Loan loan, LoanApproval approval) {
    return LoanWithApprovalResponse.builder()
        .id(loan.getId())
        .customerId(loan.getCustomer().getId())
        .plafondId(loan.getPlafondId())
        .loanAmount(loan.getAmount())
        .tenorMonth(loan.getTenureMonths())
        .interestRate(loan.getInterestRate())
        .purpose(loan.getPurpose())
        .status(loan.getStatus().toString())
        .submissionDate(loan.getSubmittedAt())
        .createdAt(loan.getCreatedAt())
        .updatedAt(loan.getUpdatedAt())
        .approvalId(approval.getId())
        .approvedBy(approval.getApprovedBy())
        .approvalStatus(approval.getApprovalStatus())
        .approvalNotes(approval.getApprovalNotes())
        .approvedAt(approval.getApprovedAt())
        .build();
  }
}
