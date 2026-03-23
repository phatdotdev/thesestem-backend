package com.dev.thesis_management.thesis.dto.group;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentResponse {
    UUID id;
    String name;
    String description;
    LocalDateTime deadline;
    String status;
}
