package com.dev.thesis_management.user.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateLecturerRequest(
        String lecturerCode,
        String fullName,
        LocalDate dob,
        String email,
        String phone,
        String address,
        String gender,
        String password,
        UUID collegeId,
        UUID facultyId,
        UUID departmentId
) {}
