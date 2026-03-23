package com.dev.thesis_management.user.dto;


import java.time.LocalDate;
import java.util.UUID;

public record CreateStudentRequest(
        String studentCode,
        String password,
        LocalDate dob,
        String gender,
        String fullName,
        String email,
        String phone,
        String address,
        UUID programId,
        UUID courseId
) {
}
