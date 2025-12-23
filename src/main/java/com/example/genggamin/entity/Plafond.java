package com.example.genggamin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity Plafond untuk mengelola produk plafond pinjaman
 * Sesuai dengan tabel plafonds di database
 */
@Entity
@Table(name = "plafonds")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plafond {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_income", nullable = false, precision = 18, scale = 2)
    private BigDecimal minIncome;

    @Column(name = "max_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "tenor_month", nullable = false)
    private Long tenorMonth;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
