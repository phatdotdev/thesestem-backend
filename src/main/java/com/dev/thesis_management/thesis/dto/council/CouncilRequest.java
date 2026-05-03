package com.dev.thesis_management.thesis.dto.council;

import java.util.List;
import java.util.UUID;

public record CouncilRequest(
        String name,
        String code,
        List<CouncilMemberRequest> members,
        UUID collegeId,
        UUID facultyId,
        UUID departmentId
) {}
