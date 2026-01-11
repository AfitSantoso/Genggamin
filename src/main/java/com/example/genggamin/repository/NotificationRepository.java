package com.example.genggamin.repository;

import com.example.genggamin.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // For fetching In-App notifications
    Page<Notification> findByUserIdAndChannelOrderByCreatedAtDesc(Long userId, com.example.genggamin.enums.NotificationChannel channel, Pageable pageable);

    // Count unread
    long countByUserIdAndChannelAndIsReadFalse(Long userId, com.example.genggamin.enums.NotificationChannel channel);

    List<Notification> findByUserIdAndChannelAndIsReadFalse(Long userId, com.example.genggamin.enums.NotificationChannel channel);
}
