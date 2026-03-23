package com.dev.thesis_management.thesis.dto.group;

import java.time.LocalDateTime;

public record CreateAssignmentRequest(
        String name,
        String description,
        LocalDateTime deadline
) {
}
