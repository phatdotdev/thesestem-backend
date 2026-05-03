package com.dev.thesis_management.thesis.dto.milestone;

import java.time.LocalDateTime;

public record MilestoneRequest(
        String title,
        String description,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
