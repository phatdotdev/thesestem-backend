package com.dev.thesis_management.thesis.dto.defense;

import java.util.UUID;

public record DefenseSearchForm(
        UUID semesterId,
        UUID councilId,
        String councilCode,
        UUID collegeId,
        UUID facultyId,
        UUID departmentId,
        UUID programId
) {
}
