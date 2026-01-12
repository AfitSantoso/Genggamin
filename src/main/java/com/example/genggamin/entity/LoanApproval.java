package com.example.genggamin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_approvals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApproval {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "loan_id", nullable = false, unique = true)
  private Long loanId;

  @Column(name = "approved_by", nullable = false)
  private Long approvedBy;

  @Column(name = "approval_status", nullable = false, length = 30)
  private String approvalStatus;

  @Column(name = "approval_notes", length = 500)
  private String approvalNotes;

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;

  @PrePersist
  protected void onCreate() {
    if (approvedAt == null) {
      approvedAt = LocalDateTime.now();
    }
  }
}
