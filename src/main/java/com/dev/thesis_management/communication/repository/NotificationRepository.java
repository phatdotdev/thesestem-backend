package com.dev.thesis_management.communication.repository;

import com.dev.thesis_management.communication.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}
