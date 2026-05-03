package com.dev.thesis_management.communication.entity;

import com.dev.thesis_management.thesis.entity.Group;
import com.dev.thesis_management.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "messages")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {
    @Id
            @GeneratedValue
    UUID id;

    @Column(columnDefinition = "TEXT")
    String content;
    LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    User sender;

    @ManyToOne
    @JoinColumn(name = "group_id")
    Group group;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    User receiver;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
