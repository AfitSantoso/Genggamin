package com.example.genggamin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanWithDisbursementResponse {
    
    // Loan fields
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
    
    // Disbursement fields
    private Long disbursementId;
    private Long disbursedBy;
    private BigDecimal disbursementAmount;
    private LocalDateTime disbursementDate;
    private String bankAccount;
    private String disbursementStatus;

    // Constructors
    public LoanWithDisbursementResponse() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getPlafondId() {
        return plafondId;
    }

    public void setPlafondId(Long plafondId) {
        this.plafondId = plafondId;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Long getTenorMonth() {
        return tenorMonth;
    }

    public void setTenorMonth(Long tenorMonth) {
        this.tenorMonth = tenorMonth;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getDisbursementId() {
        return disbursementId;
    }

    public void setDisbursementId(Long disbursementId) {
        this.disbursementId = disbursementId;
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

    public String getDisbursementStatus() {
        return disbursementStatus;
    }

    public void setDisbursementStatus(String disbursementStatus) {
        this.disbursementStatus = disbursementStatus;
    }
}
