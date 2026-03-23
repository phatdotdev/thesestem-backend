package com.dev.thesis_management.user.dto;

import java.util.UUID;

public record MentorSearchForm(
        String name,
        String code,
        String email,
        UUID collegeId,
        UUID facultyId,
        UUID departmentId
) {
}
