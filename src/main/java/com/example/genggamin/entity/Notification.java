package com.example.genggamin.entity;

import com.example.genggamin.enums.NotificationChannel;
import com.example.genggamin.enums.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "loan_id")
  private Loan loan;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", length = 50)
  private NotificationType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "channel", length = 20)
  private NotificationChannel channel;

  @Column(length = 150)
  private String title;

  @Column(length = 500)
  private String message;

  @Column(name = "is_read")
  @Builder.Default
  private Boolean isRead = false;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}
