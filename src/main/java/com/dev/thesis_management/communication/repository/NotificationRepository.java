package com.dev.thesis_management.communication.repository;

import com.dev.thesis_management.communication.entity.Notification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findAllByTypeAndUserIdOrderByCreatedAtDesc(Notification.Type type, UUID userId);

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("""
            update Notification n
            set n.read = true
            where n.user.id = :userId and n.read = false
            """)
    int markAllAsReadByUserId(@Param("userId") UUID userId);
}
