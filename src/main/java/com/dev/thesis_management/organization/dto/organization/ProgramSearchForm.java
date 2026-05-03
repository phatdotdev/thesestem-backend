package com.dev.thesis_management.organization.dto.organization;

import java.util.UUID;

public record ProgramSearchForm(
        UUID collegeId,
        UUID departmentId,
        UUID facultyId,
        String name
) {
}
