package com.example.genggamin.repository;

import com.example.genggamin.entity.PasswordResetToken;
import com.example.genggamin.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** Repository untuk operasi database pada tabel password_reset_tokens */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

  /** Cari token berdasarkan string token */
  Optional<PasswordResetToken> findByToken(String token);

  /** Cari token yang masih valid berdasarkan user */
  @Query(
      "SELECT p FROM PasswordResetToken p WHERE p.user = :user AND p.used = false AND p.expiredAt > :now ORDER BY p.createdAt DESC")
  Optional<PasswordResetToken> findValidTokenByUser(User user, LocalDateTime now);

  /** Hapus semua token yang sudah expired atau sudah digunakan */
  @Modifying
  @Query("DELETE FROM PasswordResetToken p WHERE p.expiredAt < :now OR p.used = true")
  void deleteExpiredOrUsedTokens(LocalDateTime now);

  /** Invalidate semua token lama dari user (set used = true) */
  @Modifying
  @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.user = :user AND p.used = false")
  void invalidateAllUserTokens(User user);
}
