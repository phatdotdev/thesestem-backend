package com.dev.thesis_management.thesis.dto.thesis;

import java.util.UUID;

public record ThesisSearchForm(
        String name,
        UUID studentId,
        UUID mentorId
) {
}
