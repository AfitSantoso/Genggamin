package com.example.genggamin.service;

import com.example.genggamin.entity.PasswordResetToken;
import com.example.genggamin.entity.User;
import com.example.genggamin.enums.NotificationType;
import com.example.genggamin.repository.PasswordResetTokenRepository;
import com.example.genggamin.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Service untuk handle forgot password dan reset password */
@Service
public class PasswordResetService {

  private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
  private static final int TOKEN_EXPIRY_HOURS = 1; // Token berlaku 1 jam

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository tokenRepository;
  private final EmailService emailService;
  private final PasswordEncoder passwordEncoder;
  private final NotificationService notificationService;

  public PasswordResetService(
      UserRepository userRepository,
      PasswordResetTokenRepository tokenRepository,
      EmailService emailService,
      PasswordEncoder passwordEncoder,
      NotificationService notificationService) {
    this.userRepository = userRepository;
    this.tokenRepository = tokenRepository;
    this.emailService = emailService;
    this.passwordEncoder = passwordEncoder;
    this.notificationService = notificationService;
  }

  /**
   * Proses forgot password - generate token dan kirim email
   *
   * @param email Email user yang lupa password
   */
  @Transactional
  public String processForgotPassword(String email) {
    // Cari user berdasarkan email — generic message to prevent user enumeration
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Jika email terdaftar, link reset password akan dikirim ke email Anda."));

    // Cek apakah user aktif
    if (!user.getIsActive()) {
      throw new RuntimeException("User tidak aktif. Silakan hubungi administrator.");
    }

    // Invalidate semua token lama dari user ini
    tokenRepository.invalidateAllUserTokens(user);

    // Generate token baru
    String token = UUID.randomUUID().toString();
    LocalDateTime expiredAt = LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS);

    // Simpan token ke database
    PasswordResetToken resetToken = new PasswordResetToken();
    resetToken.setUser(user);
    resetToken.setToken(token);
    resetToken.setExpiredAt(expiredAt);
    resetToken.setUsed(false);
    tokenRepository.save(resetToken);

    // Token is never logged for security reasons
    log.info("Password reset token generated for user ID: {}", user.getId());

    // Kirim email
    try {
      emailService.sendPasswordResetEmail(user.getEmail(), token, user.getUsername());
      log.info("Password reset email sent for user ID: {}", user.getId());
    } catch (Exception e) {
      // In development/testing, email sending might fail. We log the error but don't fail the
      // request.
      log.error(
          "Failed to send password reset email for user: {}. Error: {}",
          user.getUsername(),
          e.getMessage());
      // throw new RuntimeException("Gagal mengirim email reset password. Silakan coba lagi.", e);
    }

    // Send FORGOT_PASSWORD notification (primarily EMAIL-based, but triggers IN_APP based on
    // channel config)
    notificationService.sendNotification(user, NotificationType.FORGOT_PASSWORD, null);

    return token;
  }

  /**
   * Proses reset password dengan token
   *
   * @param token Token reset password
   * @param newPassword Password baru
   */
  @Transactional
  public void resetPassword(String token, String newPassword) {
    // Validasi password
    if (newPassword == null || newPassword.length() < 6) {
      throw new RuntimeException("Password harus minimal 6 karakter");
    }

    // Cari token di database
    PasswordResetToken resetToken =
        tokenRepository
            .findByToken(token)
            .orElseThrow(() -> new RuntimeException("Token tidak valid atau tidak ditemukan"));

    // Cek apakah token sudah digunakan
    if (resetToken.getUsed()) {
      throw new RuntimeException("Token sudah pernah digunakan");
    }

    // Cek apakah token sudah expired
    if (resetToken.isExpired()) {
      throw new RuntimeException("Token sudah kadaluarsa. Silakan request reset password lagi.");
    }

    // Update password user
    User user = resetToken.getUser();
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    // Mark token sebagai used
    resetToken.setUsed(true);
    tokenRepository.save(resetToken);

    log.info("Password successfully reset for user ID: {}", user.getId());
  }

  /**
   * Validasi token tanpa mereset password (untuk cek apakah token valid)
   *
   * @param token Token yang akan divalidasi
   * @return true jika token valid
   */
  public boolean validateToken(String token) {
    return tokenRepository.findByToken(token).map(PasswordResetToken::isValid).orElse(false);
  }

  /**
   * Scheduled task untuk cleanup token yang expired atau sudah digunakan Dijalankan setiap hari jam
   * 2 pagi
   */
  @Scheduled(cron = "0 0 2 * * ?")
  @Transactional
  public void cleanupExpiredTokens() {
    log.info("Running cleanup for expired and used password reset tokens");
    tokenRepository.deleteExpiredOrUsedTokens(LocalDateTime.now());
    log.info("Cleanup completed");
  }
}
