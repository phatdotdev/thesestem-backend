package com.dev.thesis_management.thesis.dto.defense;

import java.time.LocalDateTime;
import java.util.UUID;

public record DefenseRequest(
        UUID thesisId,
        UUID councilId,
        LocalDateTime defenseTime,
        String location
) {
}
