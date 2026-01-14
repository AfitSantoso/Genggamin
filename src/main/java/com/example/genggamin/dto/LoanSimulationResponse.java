package com.example.genggamin.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanSimulationResponse {
    private BigDecimal loanAmount;
    private Long tenorMonth;
    private BigDecimal interestRate; // Annual rate in percent
    private BigDecimal monthlyInstallment;
    private BigDecimal totalInterest;
    private BigDecimal totalPayment;
    private Long plafondId; // Reference to the used rule
}
