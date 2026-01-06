package com.example.genggamin.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @Column(name = "plafond_id")
  private Long plafondId;

  @Column(name = "loan_amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "tenor_month", nullable = false)
  private Long tenureMonths;

  @Column(name = "interest_rate")
  private BigDecimal interestRate;

  @Column(name = "purpose", length = 500)
  private String purpose;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private LoanStatus status = LoanStatus.SUBMITTED;

  @Transient private String reviewNotes;

  @Transient private String approvalNotes;

  @Transient private String disbursementNotes;

  @Column(name = "submission_date")
  @Builder.Default
  private LocalDateTime submittedAt = LocalDateTime.now();

  @Transient private LocalDateTime reviewedAt;

  @Transient private LocalDateTime approvedAt;

  @Transient private LocalDateTime disbursedAt;

  @Transient private String reviewedBy;

  @Transient private String approvedBy;

  @Transient private String disbursedBy;

  public enum LoanStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    DISBURSED
  }
}
