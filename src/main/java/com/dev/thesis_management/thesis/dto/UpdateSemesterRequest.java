package com.dev.thesis_management.thesis.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateSemesterRequest(String name, LocalDate startDate, LocalDate endDate, UUID yearId) {
}
