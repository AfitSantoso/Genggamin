package com.example.genggamin.dto;

import com.example.genggamin.entity.Loan;
import com.example.genggamin.entity.LoanReview;
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
public class LoanWithReviewResponse {
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
  private BigDecimal latitude;
  private BigDecimal longitude;

  // Review data
  private Long reviewId;
  private Long reviewedBy;
  private String reviewNotes;
  private String reviewStatus;
  private LocalDateTime reviewedAt;

  public static LoanWithReviewResponse fromEntities(Loan loan, LoanReview review) {
    return LoanWithReviewResponse.builder()
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
        .latitude(loan.getLatitude())
        .longitude(loan.getLongitude())
        .reviewId(review.getId())
        .reviewedBy(review.getReviewedBy())
        .reviewNotes(review.getReviewNotes())
        .reviewStatus(review.getReviewStatus())
        .reviewedAt(review.getReviewedAt())
        .build();
  }
}
