package com.dev.thesis_management.thesis.dto;

import com.dev.thesis_management.category.entity.AcademicYear;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SemesterResponse {
    UUID id;
    String name;
    LocalDate startDate;
    LocalDate endDate;
    String status;

    AcademicYear year;

    // STUDENTS

    // LECTURERS

    // GROUPS

    // TOPICS

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
