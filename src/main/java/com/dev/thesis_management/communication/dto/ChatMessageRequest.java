package com.dev.thesis_management.communication.dto;

import java.util.UUID;

public record ChatMessageRequest(
        UUID groupId,
        UUID receiverId,
        String content
) {
}
