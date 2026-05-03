package com.dev.thesis_management.ai_integration.dto;

import java.util.UUID;

public record AdviseSemesterRequest(
        UUID id,
        String userPrompt
) {
}
