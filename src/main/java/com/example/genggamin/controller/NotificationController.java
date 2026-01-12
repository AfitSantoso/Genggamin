package com.example.genggamin.controller;

import com.example.genggamin.dto.ApiResponse;
import com.example.genggamin.dto.NotificationResponse;
import com.example.genggamin.entity.User;
import com.example.genggamin.service.NotificationService;
import com.example.genggamin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
         return userRepository.findByUsername(authentication.getName())
                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        
        User user = getCurrentUser(authentication);
        Page<NotificationResponse> result = notificationService.getUserNotifications(user, pageable);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Notifications fetched", result));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication authentication) {
        User user = getCurrentUser(authentication);
        long count = notificationService.countUnread(user);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Unread count", count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            Authentication authentication,
            @PathVariable Long id) {
        
        User user = getCurrentUser(authentication);
        notificationService.markAsRead(id, user); // Service handles ownership check
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Marked as read", null));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        User user = getCurrentUser(authentication);
        notificationService.markAllAsRead(user);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "All marked as read", null));
    }
}
