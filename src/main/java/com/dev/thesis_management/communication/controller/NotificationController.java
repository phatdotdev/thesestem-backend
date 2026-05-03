package com.dev.thesis_management.communication.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.communication.dto.NotificationRequest;
import com.dev.thesis_management.communication.dto.NotificationResponse;
import com.dev.thesis_management.communication.service.NotificationService;
import com.dev.thesis_management.exception.BadRequestException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;
import static com.dev.thesis_management.common.utils.UUIDUtils.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationService notificationService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/system")
    public ResponseEntity<ApiResponse> notifyUsers(
            @Valid @RequestBody NotificationRequest request
            ) {
        notificationService.notifyUsers(request);
        return noContent();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/system")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getSystemNotifications(
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        if (userId == null) {
            throw new BadRequestException("Token không hợp lệ: không xác định được người dùng");
        }

        List<NotificationResponse> notifications = notificationService.getSystemNotifications(userId);
        return ok(notifications);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        if (userId == null) {
            throw new BadRequestException("Token không hợp lệ: không xác định được người dùng");
        }

        List<NotificationResponse> notifications = notificationService.getNotifications(userId);
        return ok(notifications);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse> markNotificationAsRead(
            @PathVariable UUID notificationId,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        if (userId == null) {
            throw new BadRequestException("Token không hợp lệ: không xác định được người dùng");
        }

        notificationService.markNotificationAsRead(userId, notificationId);
        return noContent();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Integer>> markAllNotificationsAsRead(
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        if (userId == null) {
            throw new BadRequestException("Token không hợp lệ: không xác định được người dùng");
        }

        int updatedCount = notificationService.markAllNotificationsAsRead(userId);
        return ok(updatedCount);
    }
}
