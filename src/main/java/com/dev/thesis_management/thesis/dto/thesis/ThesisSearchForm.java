package com.dev.thesis_management.thesis.dto.thesis;

import com.dev.thesis_management.thesis.entity.Thesis;

import java.util.UUID;

public record ThesisSearchForm(
        String name,
        UUID studentId,
        UUID mentorId,
        UUID departmentId,
        UUID facultyId,
        UUID collegeId,
        UUID programId,
        UUID semesterId,
        Thesis.Status status,
        Thesis.AccessLevel accessLevel
) {
}
