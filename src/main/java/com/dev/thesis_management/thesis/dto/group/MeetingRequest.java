package com.dev.thesis_management.thesis.dto.group;

import java.time.LocalDateTime;

public record MeetingRequest(
        LocalDateTime start,
        LocalDateTime end,
        String title,
        String description
) {
}
