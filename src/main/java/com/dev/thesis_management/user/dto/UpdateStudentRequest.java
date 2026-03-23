package com.dev.thesis_management.user.dto;

import java.util.UUID;

public record UpdateStudentRequest(
        String fullName,
        String studentCode,
        String password,
        String name,
        String email,
        String phone,
        String gender,
        String address,
        UUID programId,
        UUID courseId
        ) {
}
