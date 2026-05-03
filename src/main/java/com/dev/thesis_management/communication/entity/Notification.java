package com.dev.thesis_management.communication.entity;

import com.dev.thesis_management.user.entity.User;
import jakarta.persistence.*;
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

    @Id
    @GeneratedValue
    UUID id;
    String title;
    String content;

    @Enumerated(EnumType.STRING)
    Type type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    boolean read;

    LocalDateTime createdAt;

    public enum Type {

        // ===== SYSTEM =====
        SYSTEM_ANNOUNCEMENT,

        // ===== STUDENT =====
        STUDENT_REGISTER_MENTOR,
        STUDENT_CANCEL_REGISTER,
        STUDENT_SUBMIT_THESIS,
        STUDENT_SUBMIT_ASSIGNMENT,
        STUDENT_UPDATE_THESIS,
        STUDENT_UPLOAD_FILE,

        // ===== MENTOR =====
        MENTOR_APPROVE_REGISTER,
        MENTOR_REJECT_REGISTER,
        MENTOR_ADD_MEMBER,
        MENTOR_REMOVE_MEMBER,
        MENTOR_ASSIGN_TASK,
        MENTOR_COMMENT,

        // ===== GROUP =====
        GROUP_MEMBER_ADDED,
        GROUP_MEMBER_REMOVED,
        GROUP_UPDATED,

        // ===== THESIS =====
        THESIS_ASSIGNED_COUNCIL,
        THESIS_UPDATED,
        THESIS_APPROVED,
        THESIS_REJECTED,

        // ===== COUNCIL =====
        COUNCIL_ASSIGNED,
        COUNCIL_GRADE_UPDATED,
        COUNCIL_SCHEDULE_UPDATED,
        COUNCIL_FINAL_RESULT,

        // ===== DEADLINE =====
        DEADLINE_REMINDER,
        DEFENSE_SCHEDULE,

        // ===== MESSAGE =====
        CHAT_MESSAGE
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
