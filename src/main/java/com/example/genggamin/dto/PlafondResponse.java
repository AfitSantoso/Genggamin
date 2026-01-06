package com.example.genggamin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

/** DTO Response untuk Plafond */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlafondResponse {

  private Long id;
  private BigDecimal minIncome;
  private BigDecimal maxAmount;
  private Long tenorMonth;
  private BigDecimal interestRate;
  private Boolean isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
