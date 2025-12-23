package com.example.genggamin.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO Response untuk Plafond
 */
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
