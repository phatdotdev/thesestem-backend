package com.dev.thesis_management.communication.service;

import com.dev.thesis_management.communication.dto.NotificationRequest;
import com.dev.thesis_management.communication.dto.NotificationResponse;
import com.dev.thesis_management.communication.entity.Notification;
import com.dev.thesis_management.communication.mapper.NotificationMapper;
import com.dev.thesis_management.communication.repository.NotificationRepository;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.exception.ResourceNotFoundException;
import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

    NotificationRepository notificationRepository;
    UserRepository userRepository;
    WebSocketSender webSocketSender;

    @Transactional
    public void notifyUsers(NotificationRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new BadRequestException("Tiêu đề thông báo không được để trống");
        }

        if (request.content() == null || request.content().isBlank()) {
            throw new BadRequestException("Nội dung thông báo không được để trống");
        }

        List<UUID> ids;

        if (request.role() == null) {
            ids = new ArrayList<>(
                    userRepository.findAll()
                            .stream()
                            .map(User::getId)
                            .toList()
            );
        } else {
            ids = new ArrayList<>(
                    userRepository.findAllByRole(request.role())
                            .stream()
                            .map(User::getId)
                            .toList()
            );
        }

        List<User> users = userRepository.findAllById(ids);

        if (users.isEmpty()) {
            throw new BadRequestException("Không có người nhận phù hợp để gửi thông báo");
        }

        users.forEach(user -> notifyUser(user, request.title(), request.content(), Notification.Type.SYSTEM_ANNOUNCEMENT));
    }

    @Transactional
    public void notifyUser(User user, String title, String content, Notification.Type type){
        if (user == null || user.getId() == null) {
            throw new BadRequestException("Không xác định được người nhận thông báo");
        }

        Notification notification  = Notification.builder()
                .title(title)
                .content(content)
                .type(type)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        NotificationResponse response = NotificationMapper.toNotificationResponse(notification);

        try {
            webSocketSender.sendNotification(user.getId(), response);
        } catch (Exception ex) {
            // Keep DB notification even when realtime push fails.
            log.warn("Push websocket notification failed for userId={}: {}", user.getId(), ex.getMessage());
        }
    }

    public List<NotificationResponse> getSystemNotifications(UUID adminId) {
        if (adminId == null) {
            throw new BadRequestException("Không xác định được người dùng từ token");
        }

        return notificationRepository.findAllByTypeAndUserIdOrderByCreatedAtDesc(Notification.Type.SYSTEM_ANNOUNCEMENT, adminId).stream()
                .map(NotificationMapper::toNotificationResponse)
                .toList();
    }

    public List<NotificationResponse> getNotifications(UUID userId) {
        if (userId == null) {
            throw new BadRequestException("Không xác định được người dùng từ token");
        }

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationMapper::toNotificationResponse)
                .toList();
    }

    @Transactional
    public void markNotificationAsRead(UUID userId, UUID notificationId) {
        if (userId == null) {
            throw new BadRequestException("Không xác định được người dùng từ token");
        }
        if (notificationId == null) {
            throw new BadRequestException("Không xác định được thông báo");
        }

        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public int markAllNotificationsAsRead(UUID userId) {
        if (userId == null) {
            throw new BadRequestException("Không xác định được người dùng từ token");
        }

        return notificationRepository.markAllAsReadByUserId(userId);
    }
}
