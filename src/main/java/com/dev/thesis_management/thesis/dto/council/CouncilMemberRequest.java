package com.dev.thesis_management.thesis.dto.council;

import java.util.UUID;

public record CouncilMemberRequest(UUID id, UUID lecturerId, UUID roleId) {
}
