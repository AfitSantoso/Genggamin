package com.example.genggamin.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/** Service untuk mengirim email menggunakan Spring Mail */
@Service
public class EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Value("${app.frontend.url:https://genggamin-fe.vercel.app/}")
  private String frontendUrl;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /** Mask email for secure logging: show first 2 chars + ***@domain */
  private String maskEmail(String email) {
    if (email == null || !email.contains("@")) return "***";
    int atIndex = email.indexOf('@');
    String prefix = email.substring(0, Math.min(2, atIndex));
    return prefix + "***" + email.substring(atIndex);
  }

  /**
   * Mengirim email reset password
   *
   * @param toEmail Email penerima
   * @param token Token reset password
   * @param username Username penerima
   */
  public void sendPasswordResetEmail(String toEmail, String token, String username) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject("Reset Password - Genggamin Loan System");

      String resetUrl = frontendUrl + "/reset-password?token=" + token;

      String htmlContent = buildPasswordResetEmailTemplate(username, resetUrl);
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("Password reset email sent successfully to: {}", maskEmail(toEmail));

    } catch (MessagingException e) {
      log.error("Failed to send password reset email to: {}", maskEmail(toEmail), e);
      throw new RuntimeException("Failed to send password reset email", e);
    }
  }

  /**
   * Mengirim email konfirmasi registrasi
   *
   * @param toEmail Email user
   * @param username Username user
   */
  public void sendRegistrationConfirmationEmail(String toEmail, String username) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject("Selamat Datang di Genggamin Loan System");

      String loginUrl = frontendUrl + "/login";
      String htmlContent = buildRegistrationEmailTemplate(username, loginUrl);
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("Registration confirmation email sent to: {}", maskEmail(toEmail));

    } catch (MessagingException e) {
      log.error("Failed to send registration email to: {}", maskEmail(toEmail), e);
      // Don't throw exception to avoid rollback of registration
    }
  }

  /**
   * Mengirim email pengajuan diterima
   *
   * @param toEmail Email user
   * @param username Username user
   * @param loanId ID pinjaman
   * @param amount Jumlah pinjaman
   */
  public void sendLoanApprovedEmail(
      String toEmail, String username, Long loanId, java.math.BigDecimal amount) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject("Pengajuan Pinjaman Disetujui - Genggamin");

      String htmlContent = buildLoanApprovedEmailTemplate(username, loanId, amount);
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("Loan approved email sent to: {}", maskEmail(toEmail));

    } catch (MessagingException e) {
      log.error("Failed to send loan approved email to: {}", maskEmail(toEmail), e);
    }
  }

  /**
   * Mengirim email pengajuan ditolak
   *
   * @param toEmail Email user
   * @param username Username user
   * @param loanId ID pinjaman
   * @param reason Alasan penolakan
   */
  public void sendLoanRejectedEmail(String toEmail, String username, Long loanId, String reason) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject("Update Status Pengajuan Pinjaman - Genggamin");

      String htmlContent = buildLoanRejectedEmailTemplate(username, loanId, reason);
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("Loan rejected email sent to: {}", maskEmail(toEmail));

    } catch (MessagingException e) {
      log.error("Failed to send loan rejected email to: {}", maskEmail(toEmail), e);
    }
  }

  /**
   * Mengirim email dana dicairkan
   *
   * @param toEmail Email user
   * @param username Username user
   * @param loanId ID pinjaman
   * @param amount Jumlah pencairan
   * @param bankAccount Rekening tujuan
   */
  public void sendLoanDisbursedEmail(
      String toEmail,
      String username,
      Long loanId,
      java.math.BigDecimal amount,
      String bankAccount) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject("Dana Pinjaman Telah Dicairkan - Genggamin");

      String htmlContent = buildLoanDisbursedEmailTemplate(username, loanId, amount, bankAccount);
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("Loan disbursed email sent to: {}", maskEmail(toEmail));

    } catch (MessagingException e) {
      log.error("Failed to send loan disbursed email to: {}", maskEmail(toEmail), e);
    }
  }

  private String formatRupiah(java.math.BigDecimal amount) {
    java.text.NumberFormat format =
        java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("id", "ID"));
    return format.format(amount);
  }

  private String buildEmailTemplate(String title, String content) {
    return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2E7D32; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #ffffff; padding: 30px; border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 8px 8px; }
                    .footer { text-align: center; margin-top: 20px; color: #757575; font-size: 12px; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #2E7D32; color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; font-weight: bold; }
                    .info-box { background-color: #f5f5f5; padding: 15px; border-left: 4px solid #2E7D32; margin: 15px 0; border-radius: 4px; }
                    .alert-box { background-color: #ffebee; padding: 15px; border-left: 4px solid #c62828; margin: 15px 0; border-radius: 4px; }
                    h1 { margin: 0; font-size: 24px; }
                    h2 { color: #2E7D32; font-size: 20px; margin-top: 0; }
                    ul { list-style-type: none; padding: 0; }
                    li { margin-bottom: 10px; padding-left: 20px; position: relative; }
                    li:before { content: "•"; color: #2E7D32; font-weight: bold; position: absolute; left: 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>"""
        + title
        + """
                        </h1>
                    </div>
                    <div class="content">
                        """
        + content
        + """
                    </div>
                    <div class="footer">
                        <p>&copy; 2026 Genggamin Loan System. All rights reserved.</p>
                        <p>Jangan balas email ini. Ini adalah email otomatis.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
  }

  private String buildRegistrationEmailTemplate(String username, String loginUrl) {
    String content =
        String.format(
            """
            <p>Halo <strong>%s</strong>,</p>
            <p>Terima kasih telah mendaftar di Genggamin. Akun Anda berhasil dibuat.</p>
            <p>Silakan login untuk mulai mengajukan pinjaman.</p>
            <p style="text-align: center;">
                <a href="%s" class="button">Masuk ke Aplikasi</a>
            </p>
            """,
            username, loginUrl);
    return buildEmailTemplate("Registrasi Berhasil", content);
  }

  private String buildLoanApprovedEmailTemplate(
      String username, Long loanId, java.math.BigDecimal amount) {
    String content =
        String.format(
            """
            <p>Halo <strong>%s</strong>,</p>
            <div class="info-box">
                <h2>Kabar Gembira! 🎉</h2>
                <p>Pengajuan pinjaman Anda telah <strong>DISETUJUI</strong>.</p>
            </div>
            <p>Detail Pinjaman:</p>
            <ul>
                <li>ID Pengajuan: <strong>#%d</strong></li>
                <li>Jumlah Disetujui: <strong>%s</strong></li>
            </ul>
            <p>Pencairan dana akan segera diproses oleh tim kami. Harap pantau terus statusnya di aplikasi.</p>
            """,
            username, loanId, formatRupiah(amount));
    return buildEmailTemplate("Pengajuan Disetujui", content);
  }

  private String buildLoanRejectedEmailTemplate(String username, Long loanId, String reason) {
    String content =
        String.format(
            """
            <p>Halo <strong>%s</strong>,</p>
            <div class="alert-box">
                <h2>Status Pengajuan</h2>
                <p>Mohon maaf, pengajuan pinjaman Anda <strong>BELUM DISETUJUI</strong> saat ini.</p>
            </div>
            <p>Detail:</p>
            <ul>
                <li>ID Pengajuan: <strong>#%d</strong></li>
                <li>Alasan: %s</li>
            </ul>
            <p>Anda dapat mencoba mengajukan kembali di lain kesempatan atau menghubungi CS kami untuk informasi lebih lanjut.</p>
            """,
            username, loanId, reason != null ? reason : "Tidak memenuhi kriteria");
    return buildEmailTemplate("Pengajuan Ditolak", content);
  }

  private String buildLoanDisbursedEmailTemplate(
      String username, Long loanId, java.math.BigDecimal amount, String bankAccount) {
    String content =
        String.format(
            """
            <p>Halo <strong>%s</strong>,</p>
            <div class="info-box">
                <h2>Dana Telah Dicairkan 💰</h2>
                <p>Dana pinjaman Anda telah berhasil ditransfer.</p>
            </div>
            <p>Detail Transaksi:</p>
            <ul>
                <li>ID Pengajuan: <strong>#%d</strong></li>
                <li>Jumlah Cair: <strong>%s</strong></li>
                <li>Tujuan Transfer: <strong>%s</strong></li>
            </ul>
            <p>Silakan cek rekening Anda secara berkala.</p>
            <p>Jangan lupa untuk melakukan pembayaran cicilan tepat waktu ya!</p>
            """,
            username, loanId, formatRupiah(amount), bankAccount);
    return buildEmailTemplate("Dana Dicairkan", content);
  }

  /** Build HTML template untuk email reset password */
  private String buildPasswordResetEmailTemplate(String username, String resetUrl) {
    return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 30px; border: 1px solid #ddd; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .token { background-color: #f0f0f0; padding: 10px; border-left: 4px solid #4CAF50; margin: 15px 0; font-family: monospace; word-break: break-all; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    .warning { color: #d32f2f; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Reset Password</h1>
                    </div>
                    <div class="content">
                        <p>Halo <strong>%s</strong>,</p>

                        <p>Kami menerima permintaan untuk reset password akun Anda di Genggamin Loan System.</p>

                        <p>Silakan klik tombol di bawah ini untuk mereset password Anda:</p>

                        <p style="text-align: center;">
                            <a href="%s" class="button">Reset Password</a>
                        </p>

                        <p>Atau copy dan paste link berikut di browser Anda:</p>
                        <div class="token">%s</div>

                        <p class="warning">⚠️ Link ini hanya berlaku selama 1 jam.</p>

                        <p>Jika Anda tidak melakukan permintaan reset password, abaikan email ini. Password Anda tidak akan berubah.</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 Genggamin Loan System. All rights reserved.</p>
                        <p>Email ini dikirim secara otomatis. Mohon tidak membalas email ini.</p>
                    </div>
                </div>
            </body>
            </html>
            """
        .formatted(username, resetUrl, resetUrl);
  }
}
