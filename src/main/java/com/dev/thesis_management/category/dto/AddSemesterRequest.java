package com.dev.thesis_management.category.dto;

import java.time.LocalDate;

public record AddSemesterRequest(
        String name,
        LocalDate startDate,
        LocalDate endDate) {
}
