package com.dev.thesis_management.thesis.dto.group;

import com.dev.thesis_management.user.dto.StudentResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopicResponse {
    UUID id;
    String title;
    String description;
    Integer maxStudents;
    Integer currentStudents;
    String status;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
