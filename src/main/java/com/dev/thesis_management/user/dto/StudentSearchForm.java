package com.dev.thesis_management.user.dto;

import java.util.UUID;

public record StudentSearchForm(
        String name,
        String code,
        String email,
        UUID programId,
        UUID courseId
) {
}
