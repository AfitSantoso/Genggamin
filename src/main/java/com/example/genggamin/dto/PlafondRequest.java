package com.example.genggamin.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO Request untuk create dan update Plafond
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlafondRequest {

    private BigDecimal minIncome;
    private BigDecimal maxAmount;
    private Long tenorMonth;
    private BigDecimal interestRate;
    private Boolean isActive;

    /**
     * Validasi business rules
     */
    public void validate() {
        if (minIncome == null || minIncome.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Min income must be greater than 0");
        }
        if (maxAmount == null || maxAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Max amount must be greater than 0");
        }
        if (tenorMonth == null || tenorMonth <= 0) {
            throw new IllegalArgumentException("Tenor month must be greater than 0");
        }
        if (tenorMonth > 360) {
            throw new IllegalArgumentException("Tenor month cannot exceed 360 months (30 years)");
        }
        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate must be 0 or greater");
        }
        if (interestRate.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Interest rate cannot exceed 100%");
        }
    }
}
