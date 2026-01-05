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

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
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

      String htmlContent = buildPasswordResetEmailTemplate(username, resetUrl, token);
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("Password reset email sent successfully to: {}", toEmail);

    } catch (MessagingException e) {
      log.error("Failed to send password reset email to: {}", toEmail, e);
      throw new RuntimeException("Failed to send password reset email", e);
    }
  }

  /** Build HTML template untuk email reset password */
  private String buildPasswordResetEmailTemplate(String username, String resetUrl, String token) {
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

                        <hr>

                        <p><strong>Token untuk testing API:</strong></p>
                        <div class="token">%s</div>
                        <p style="font-size: 12px; color: #666;">Gunakan token ini di endpoint: POST /auth/reset-password</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Genggamin Loan System. All rights reserved.</p>
                        <p>Email ini dikirim secara otomatis. Mohon tidak membalas email ini.</p>
                    </div>
                </div>
            </body>
            </html>
            """
        .formatted(username, resetUrl, resetUrl, token);
  }
}
