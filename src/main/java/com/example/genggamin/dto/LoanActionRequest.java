package com.example.genggamin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanActionRequest {
  private String notes;
  private Boolean approved; // For approval action: true = approve, false = reject
}
