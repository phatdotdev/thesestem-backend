package com.dev.thesis_management.communication.entity;

import com.dev.thesis_management.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "notifications")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {

    UUID id;
    String title;
    String content;

    Type type;

    User user;

    boolean read;

    LocalDateTime createdAt;

    public enum Type {
        INFO,
        WARNING,
        ERROR,
        SUCCESS,
        MESSAGE
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
