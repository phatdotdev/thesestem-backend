package com.dev.thesis_management.communication.service;

import com.dev.thesis_management.communication.dto.NotificationResponse;
import com.dev.thesis_management.communication.entity.Notification;
import com.dev.thesis_management.communication.mapper.NotificationMapper;
import com.dev.thesis_management.communication.repository.NotificationRepository;
import com.dev.thesis_management.user.entity.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

    NotificationRepository notificationRepository;

    WebSocketSender webSocketSender;

    public void notifyUser(User user, String title, String content, Notification.Type type){

        Notification notification  = Notification.builder()
                .title(title)
                .content(content)
                .type(type)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        NotificationResponse response = NotificationMapper.toNotificationResponse(notification);

        webSocketSender.sendNotification(user.getId(), response);
    }
}
