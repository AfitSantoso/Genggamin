package com.example.genggamin.service;

import com.example.genggamin.dto.NotificationResponse;
import com.example.genggamin.entity.Loan;
import com.example.genggamin.entity.Notification;
import com.example.genggamin.entity.User;
import com.example.genggamin.enums.NotificationChannel;
import com.example.genggamin.enums.NotificationType;
import com.example.genggamin.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
        log.info("[PUSH] To UserID: {}, Title: {}, Message: {}", user.getId(), title, message);
    }

    /**
     * Main entry point to trigger notifications based on business events.
     * Logic for channel selection is embedded here based on requirements.
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

    private void saveInAppNotification(User user, NotificationType type, String title, String message, Loan loan) {
        Notification notification = Notification.builder()
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

    /**
     * Determine which channels to use based on the Event Type (Process Matrix).
     */
    private List<NotificationChannel> determineChannels(NotificationType type) {
        List<NotificationChannel> channels = new ArrayList<>();

        switch (type) {
            // --- CUSTOMER ---
            case REGISTER:
            case FORGOT_PASSWORD:
                channels.add(NotificationChannel.EMAIL);
                break;
            case LOAN_SUBMISSION: // Submit Pengajuan -> InApp only
            case LOAN_REJECTED:   // Pengajuan Ditolak -> Email + InApp
                channels.add(NotificationChannel.IN_APP);
                if (type == NotificationType.LOAN_REJECTED) {
                    channels.add(NotificationChannel.EMAIL);
                }
                break;
            case LOAN_APPROVED:   // Pengajuan Disetujui -> Email + Push + InApp
            case LOAN_DISBURSED:  // Dana Dicairkan -> Email + Push + InApp
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
        // Simple templates
        String loanStr = (loan != null) ? " (ID: " + loan.getId() + ")" : "";
        return switch (type) {
            case REGISTER -> "Please confirm your email to activate account.";
            case FORGOT_PASSWORD -> "Click the link in email to reset password.";
            case LOAN_SUBMISSION -> "Your loan application has been received and is under review." + loanStr;
            case LOAN_APPROVED -> "Good news! Your loan application has been approved." + loanStr;
            case LOAN_REJECTED -> "We regret to inform you that your loan application was not approved." + loanStr;
            case LOAN_DISBURSED -> "The funds have been sent to your registered bank account." + loanStr;
            case LOAN_NEW -> "A new loan application needs review." + loanStr;
            case REVIEW_COMPLETED -> "You have completed the review." + loanStr;
            case READY_FOR_APPROVAL -> "A loan application is ready for your approval." + loanStr;
            case APPROVAL_COMPLETED -> "You have processed the approval." + loanStr;
            case READY_FOR_DISBURSEMENT -> "A loan is approved and ready for disbursement." + loanStr;
            case DISBURSEMENT_COMPLETED -> "Disbursement recorded successfully." + loanStr;
        };
    }

    // --- API for Controller ---

    public Page<NotificationResponse> getUserNotifications(User user, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUserIdAndChannelOrderByCreatedAtDesc(
                user.getId(), NotificationChannel.IN_APP, pageable);
        
        return page.map(this::mapToResponse);
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
             throw new RuntimeException("Unauthorized");
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserIdAndChannelAndIsReadFalse(
                user.getId(), NotificationChannel.IN_APP);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }
    
    public long countUnread(User user) {
        return notificationRepository.countByUserIdAndChannelAndIsReadFalse(user.getId(), NotificationChannel.IN_APP);
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
