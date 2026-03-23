package com.dev.thesis_management.thesis.dto.thesis;

import com.dev.thesis_management.thesis.entity.Thesis;

public record ThesisRequest(
        String title,
        String titleEn,
        String description,
        String descriptionEn,
        Thesis.Status status,
        Integer progressPercent
) {
}
