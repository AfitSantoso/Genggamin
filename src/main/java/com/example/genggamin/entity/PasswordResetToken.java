package com.example.genggamin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity untuk tabel password_reset_tokens Menyimpan token reset password dengan expiration time
 */
@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(name = "expired_at", nullable = false)
  private LocalDateTime expiredAt;

  @Column(nullable = false)
  private Boolean used = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  /** Cek apakah token sudah expired */
  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiredAt);
  }

  /** Cek apakah token valid (belum expired dan belum digunakan) */
  public boolean isValid() {
    return !isExpired() && !used;
  }
}
