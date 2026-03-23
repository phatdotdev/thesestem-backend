package com.dev.thesis_management.thesis.dto.group;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateAssignmentRequest(
        String name,
        String description,
        LocalDateTime deadline
) {
}
