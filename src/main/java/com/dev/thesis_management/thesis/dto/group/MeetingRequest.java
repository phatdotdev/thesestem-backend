package com.dev.thesis_management.thesis.dto.group;

import java.time.LocalDateTime;

public record MeetingRequest(
        LocalDateTime startAt,
        LocalDateTime endAt,
        String title,
        String description
) {
}
