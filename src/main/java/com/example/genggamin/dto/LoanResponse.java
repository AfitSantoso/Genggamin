package com.example.genggamin.dto;

import com.example.genggamin.entity.Loan;
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
public class LoanResponse {
  private Long id;
  private String username;
  private Long plafondId;
  private BigDecimal amount;
  private Long tenureMonths;
  private BigDecimal interestRate;
  private String purpose;
  private String status;
  private String reviewNotes;
  private String approvalNotes;
  private String disbursementNotes;
  private LocalDateTime submittedAt;
  private LocalDateTime reviewedAt;
  private LocalDateTime approvedAt;
  private LocalDateTime disbursedAt;
  private String reviewedBy;
  private String approvedBy;
  private String disbursedBy;

  public static LoanResponse fromEntity(Loan loan) {
    return LoanResponse.builder()
        .id(loan.getId())
        .username(loan.getCustomer().getUser().getUsername())
        .plafondId(loan.getPlafondId())
        .amount(loan.getAmount())
        .tenureMonths(loan.getTenureMonths())
        .interestRate(loan.getInterestRate())
        .purpose(loan.getPurpose())
        .status(loan.getStatus().name())
        .reviewNotes(loan.getReviewNotes())
        .approvalNotes(loan.getApprovalNotes())
        .disbursementNotes(loan.getDisbursementNotes())
        .submittedAt(loan.getSubmittedAt())
        .reviewedAt(loan.getReviewedAt())
        .approvedAt(loan.getApprovedAt())
        .disbursedAt(loan.getDisbursedAt())
        .reviewedBy(loan.getReviewedBy())
        .approvedBy(loan.getApprovedBy())
        .disbursedBy(loan.getDisbursedBy())
        .build();
  }
}
