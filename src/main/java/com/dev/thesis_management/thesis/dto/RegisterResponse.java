package com.dev.thesis_management.thesis.dto;

import com.dev.thesis_management.thesis.entity.MentorRegister;
import com.dev.thesis_management.user.dto.LecturerResponse;
import com.dev.thesis_management.user.dto.StudentResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterResponse {
    UUID id;
    StudentResponse student;
    LecturerResponse mentor;
    String message;
    MentorRegister.Status status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
