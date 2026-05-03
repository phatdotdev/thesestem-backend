package com.dev.thesis_management.thesis.dto.milestone;

import java.time.LocalDateTime;
import java.util.UUID;

public record MilestoneResponse(
        UUID id,
        String title,
        String description,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
