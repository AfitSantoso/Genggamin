package com.example.genggamin.service;

import com.example.genggamin.dto.NotificationResponse;
import com.example.genggamin.entity.Loan;
import com.example.genggamin.entity.Notification;
import com.example.genggamin.entity.User;
import com.example.genggamin.enums.NotificationChannel;
import com.example.genggamin.enums.NotificationType;
import com.example.genggamin.repository.NotificationRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final NotificationRepository notificationRepository;

  // Simulate Email Service
  private void sendEmail(User user, String title, String message) {
    log.info("[EMAIL] To: {}, Title: {}, Body: {}", user.getEmail(), title, message);
  }

  // Simulate Push Notification Service (Firebase/OneSignal)
  private void sendPushNotification(User user, String title, String message) {
    if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
      log.warn("User {} has no FCM token, skipping push notification", user.getUsername());
      return;
    }

    try {
      com.google.firebase.messaging.Notification notification =
          com.google.firebase.messaging.Notification.builder()
              .setTitle(title)
              .setBody(message)
              .build();

      Message msg =
          Message.builder().setToken(user.getFcmToken()).setNotification(notification).build();

      String response = FirebaseMessaging.getInstance().send(msg);
      log.info("[PUSH] Sent to UserID: {}, Response: {}", user.getId(), response);
    } catch (Exception e) {
      log.error("Error sending push notification: {}", e.getMessage());
    }
  }

  /**
   * Main entry point to trigger notifications based on business events. Logic for channel selection
   * is embedded here based on requirements.
   */
  @Transactional
  public void sendNotification(User user, NotificationType type, Loan loan) {
    if (user == null) {
      log.warn("Cannot send notification to null user");
      return;
    }

    List<NotificationChannel> channels = determineChannels(type);
    String title = generateTitle(type);
    String message = generateMessage(type, loan);

    for (NotificationChannel channel : channels) {
      switch (channel) {
        case EMAIL:
          sendEmail(user, title, message);
          break;
        case PUSH:
          sendPushNotification(user, title, message);
          break;
        case IN_APP:
          saveInAppNotification(user, type, title, message, loan);
          break;
      }
    }
  }

  private void saveInAppNotification(
      User user, NotificationType type, String title, String message, Loan loan) {
    Notification notification =
        Notification.builder()
            .user(user)
            .loan(loan)
            .type(type)
            .channel(NotificationChannel.IN_APP)
            .title(title)
            .message(message)
            .isRead(false)
            .build();
    notificationRepository.save(notification);
    log.info("[IN-APP] Saved notification for UserID: {}", user.getId());
  }

  /** Determine which channels to use based on the Event Type (Process Matrix). */
  private List<NotificationChannel> determineChannels(NotificationType type) {
    List<NotificationChannel> channels = new ArrayList<>();

    switch (type) {
        // --- CUSTOMER ---
      case REGISTER:
      case FORGOT_PASSWORD:
        channels.add(NotificationChannel.EMAIL);
        channels.add(
            NotificationChannel.IN_APP); // Also save to database for frontend/mobile display
        break;
      case LOAN_SUBMISSION: // Submit Pengajuan -> InApp only
      case LOAN_REJECTED: // Pengajuan Ditolak -> Email + InApp
        channels.add(NotificationChannel.IN_APP);
        if (type == NotificationType.LOAN_REJECTED) {
          channels.add(NotificationChannel.EMAIL);
        }
        break;
      case LOAN_APPROVED: // Pengajuan Disetujui -> Email + Push + InApp
      case LOAN_DISBURSED: // Dana Dicairkan -> Email + Push + InApp
        channels.add(NotificationChannel.EMAIL);
        channels.add(NotificationChannel.PUSH);
        channels.add(NotificationChannel.IN_APP);
        break;

        // --- MARKETING ---
      case LOAN_NEW: // Pengajuan Baru -> Push + InApp
        channels.add(NotificationChannel.PUSH);
        channels.add(NotificationChannel.IN_APP);
        break;
      case REVIEW_COMPLETED: // Review Selesai -> InApp
        channels.add(NotificationChannel.IN_APP);
        break;

        // --- BRANCH MANAGER ---
      case READY_FOR_APPROVAL: // Siap Approve -> Push + InApp
        channels.add(NotificationChannel.PUSH);
        channels.add(NotificationChannel.IN_APP);
        break;
      case APPROVAL_COMPLETED: // Approve Selesai -> InApp
        channels.add(NotificationChannel.IN_APP);
        break;

        // --- BACKOFFICE ---
      case READY_FOR_DISBURSEMENT: // Siap Dicairkan -> Push + InApp
        channels.add(NotificationChannel.PUSH);
        channels.add(NotificationChannel.IN_APP);
        break;
      case DISBURSEMENT_COMPLETED: // Cair Selesai (Internal confirmation) -> InApp
        channels.add(NotificationChannel.IN_APP);
        break;
    }
    return channels;
  }

  private String generateTitle(NotificationType type) {
    // Can be moved to messages.properties
    return switch (type) {
      case REGISTER -> "Welcome to Genggamin!";
      case FORGOT_PASSWORD -> "Reset Your Password";
      case LOAN_SUBMISSION -> "Loan Submitted";
      case LOAN_APPROVED -> "Loan Approved!";
      case LOAN_REJECTED -> "Loan Application Update";
      case LOAN_DISBURSED -> "Funds Disbursed";
      case LOAN_NEW -> "New Loan Application";
      case REVIEW_COMPLETED -> "Review Completed";
      case READY_FOR_APPROVAL -> "Loan Ready for Approval";
      case APPROVAL_COMPLETED -> "Approval Done";
      case READY_FOR_DISBURSEMENT -> "Loan Ready for Disbursement";
      case DISBURSEMENT_COMPLETED -> "Disbursement Completed";
    };
  }

  private String generateMessage(NotificationType type, Loan loan) {
    // Template sederhana
    String loanStr = (loan != null) ? " (ID: " + loan.getId() + ")" : "";
    return switch (type) {
      case REGISTER -> "Silakan konfirmasi email Anda untuk mengaktifkan akun.";
      case FORGOT_PASSWORD -> "Klik tautan di email untuk mengatur ulang kata sandi Anda.";
      case LOAN_SUBMISSION ->
          "Permohonan pinjaman Anda telah diterima dan sedang dalam proses peninjauan." + loanStr;
      case LOAN_APPROVED -> "Kabar baik! Permohonan pinjaman Anda telah disetujui." + loanStr;
      case LOAN_REJECTED ->
          "Mohon maaf, permohonan pinjaman Anda belum dapat disetujui saat ini." + loanStr;
      case LOAN_DISBURSED ->
          "Dana telah dikirimkan ke rekening bank yang Anda daftarkan." + loanStr;
      case LOAN_NEW -> "Ada permohonan pinjaman baru yang perlu ditinjau." + loanStr;
      case REVIEW_COMPLETED -> "Anda telah menyelesaikan proses peninjauan." + loanStr;
      case READY_FOR_APPROVAL ->
          "Aplikasi pinjaman siap untuk mendapatkan persetujuan Anda." + loanStr;
      case APPROVAL_COMPLETED -> "Anda telah menyelesaikan proses persetujuan." + loanStr;
      case READY_FOR_DISBURSEMENT -> "Pinjaman telah disetujui dan siap untuk dicairkan." + loanStr;
      case DISBURSEMENT_COMPLETED -> "Pencairan dana telah berhasil dicatat." + loanStr;
    };
  }

  // --- API for Controller ---

  public Page<NotificationResponse> getUserNotifications(User user, Pageable pageable) {
    Page<Notification> page =
        notificationRepository.findByUserIdAndChannelOrderByCreatedAtDesc(
            user.getId(), NotificationChannel.IN_APP, pageable);

    return page.map(this::mapToResponse);
  }

  @Transactional
  public void markAsRead(Long notificationId, User user) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

    if (!notification.getUser().getId().equals(user.getId())) {
      throw new RuntimeException("Unauthorized");
    }

    notification.setIsRead(true);
    notificationRepository.save(notification);
  }

  @Transactional
  public void markAllAsRead(User user) {
    List<Notification> unread =
        notificationRepository.findByUserIdAndChannelAndIsReadFalse(
            user.getId(), NotificationChannel.IN_APP);
    unread.forEach(n -> n.setIsRead(true));
    notificationRepository.saveAll(unread);
  }

  public long countUnread(User user) {
    return notificationRepository.countByUserIdAndChannelAndIsReadFalse(
        user.getId(), NotificationChannel.IN_APP);
  }

  private NotificationResponse mapToResponse(Notification n) {
    return NotificationResponse.builder()
        .id(n.getId())
        .type(n.getType())
        .title(n.getTitle())
        .message(n.getMessage())
        .isRead(n.getIsRead())
        .createdAt(n.getCreatedAt())
        .loanId(n.getLoan() != null ? n.getLoan().getId() : null)
        .build();
  }
}
