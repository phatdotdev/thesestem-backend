package com.dev.thesis_management.thesis.dto.defense;

import java.util.UUID;

public record DefenseSearchForm(
        UUID councilId,
        String councilCode
) {
}
