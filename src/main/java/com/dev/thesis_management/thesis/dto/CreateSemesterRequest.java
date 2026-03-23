package com.dev.thesis_management.thesis.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateSemesterRequest {
    String name;
    LocalDate startDate;
    LocalDate endDate;
    UUID yearId;
}
