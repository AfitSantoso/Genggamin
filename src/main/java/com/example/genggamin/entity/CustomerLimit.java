package com.example.genggamin.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "customer_limits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerLimit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plafond_id", nullable = false)
  private Plafond plafond;

  @Column(name = "total_limit", nullable = false, precision = 18, scale = 2)
  private BigDecimal totalLimit;

  @Column(name = "available_limit", nullable = false, precision = 18, scale = 2)
  private BigDecimal availableLimit;

  @Column(name = "is_locked", nullable = false)
  @Builder.Default
  private Boolean isLocked = false;

  @Column(name = "updated_at")
  @org.hibernate.annotations.UpdateTimestamp
  private LocalDateTime updatedAt;
}
