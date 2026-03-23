package com.dev.thesis_management.communication.mapper;

import com.dev.thesis_management.communication.dto.NotificationResponse;
import com.dev.thesis_management.communication.entity.Notification;

public class NotificationMapper {
    public static NotificationResponse toNotificationResponse(Notification notification){
        return NotificationResponse.builder().build();
    }
}
