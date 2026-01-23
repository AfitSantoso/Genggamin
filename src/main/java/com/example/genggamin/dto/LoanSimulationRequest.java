package com.example.genggamin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanSimulationRequest {
  @NotNull(message = "Loan amount is required") @DecimalMin(value = "1.0", message = "Loan amount must be greater than 0")
  private BigDecimal amount;

  @NotNull(message = "Tenor is required") private Long tenor;

  // Optional: if user selects a specific plafond
  private Long plafondId;
}
