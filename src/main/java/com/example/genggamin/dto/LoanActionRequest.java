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
  private String action; // For review: "APPROVE" or "REJECT", For disbursement: "DISBURSE"
  private String notes;
  private Boolean approved; // For approval action: true = approve, false = reject
  private String bankAccount; // For disbursement: nomor rekening tujuan pencairan
}
