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
public class LoanRequest {
  private Long plafondId;
  private BigDecimal amount;
  private Long tenureMonths;
  private String purpose;
  private BigDecimal latitude;
  private BigDecimal longitude;
}
