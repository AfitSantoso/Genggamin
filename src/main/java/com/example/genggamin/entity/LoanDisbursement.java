package com.example.genggamin.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_disbursements")
public class LoanDisbursement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "loan_id", nullable = false, unique = true)
  private Long loanId;

  @Column(name = "disbursed_by", nullable = false)
  private Long disbursedBy;

  @Column(name = "disbursement_amount", nullable = false, precision = 18, scale = 2)
  private BigDecimal disbursementAmount;

  @Column(name = "disbursement_date")
  private LocalDateTime disbursementDate;

  @Column(name = "bank_account", length = 100)
  private String bankAccount;

  @Column(name = "status", length = 30)
  private String status;

  @PrePersist
  protected void onCreate() {
    disbursementDate = LocalDateTime.now();
  }

  // Constructors
  public LoanDisbursement() {}

  public LoanDisbursement(
      Long loanId,
      Long disbursedBy,
      BigDecimal disbursementAmount,
      String bankAccount,
      String status) {
    this.loanId = loanId;
    this.disbursedBy = disbursedBy;
    this.disbursementAmount = disbursementAmount;
    this.bankAccount = bankAccount;
    this.status = status;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getLoanId() {
    return loanId;
  }

  public void setLoanId(Long loanId) {
    this.loanId = loanId;
  }

  public Long getDisbursedBy() {
    return disbursedBy;
  }

  public void setDisbursedBy(Long disbursedBy) {
    this.disbursedBy = disbursedBy;
  }

  public BigDecimal getDisbursementAmount() {
    return disbursementAmount;
  }

  public void setDisbursementAmount(BigDecimal disbursementAmount) {
    this.disbursementAmount = disbursementAmount;
  }

  public LocalDateTime getDisbursementDate() {
    return disbursementDate;
  }

  public void setDisbursementDate(LocalDateTime disbursementDate) {
    this.disbursementDate = disbursementDate;
  }

  public String getBankAccount() {
    return bankAccount;
  }

  public void setBankAccount(String bankAccount) {
    this.bankAccount = bankAccount;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
