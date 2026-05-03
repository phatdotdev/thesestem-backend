package com.dev.thesis_management.statistics.dto.semester;

import java.util.UUID;

public record OrganizationStatisticsRequest(
        UUID semesterId,
        UUID collegeId,
        UUID facultyId,
        UUID departmentId
) {
}
