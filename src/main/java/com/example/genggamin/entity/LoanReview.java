package com.example.genggamin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanReview {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "loan_id", nullable = false, unique = true)
  private Long loanId;

  @Column(name = "reviewed_by", nullable = false)
  private Long reviewedBy;

  @Column(name = "review_notes", length = 500)
  private String reviewNotes;

  @Column(name = "review_status", nullable = false, length = 30)
  private String reviewStatus;

  @Column(name = "reviewed_at")
  private LocalDateTime reviewedAt;

  @PrePersist
  protected void onCreate() {
    if (reviewedAt == null) {
      reviewedAt = LocalDateTime.now();
    }
  }
}
