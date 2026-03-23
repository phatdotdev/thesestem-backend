package com.dev.thesis_management.thesis.dto;

import java.util.UUID;

public record CreateRegisterRequest(
        UUID mentorId,
        String message
) {
}
