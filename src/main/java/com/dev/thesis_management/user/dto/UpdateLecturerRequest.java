package com.dev.thesis_management.user.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateLecturerRequest(
        String lecturerCode,
        String fullName,
        LocalDate dob,
        String email,
        String password,
        String address,
        String phone,
        UUID collegeId,
        UUID departmentId,
        UUID facultyId
) {
}
