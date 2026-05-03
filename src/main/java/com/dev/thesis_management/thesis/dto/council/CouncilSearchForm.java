package com.dev.thesis_management.thesis.dto.council;

import java.util.UUID;

public record CouncilSearchForm(
        String name,
        String code,
        UUID collegeId,
        UUID facultyId,
        UUID departmentId
) {
}
