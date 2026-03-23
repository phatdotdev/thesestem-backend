package com.dev.thesis_management.user.dto;

import com.dev.thesis_management.category.entity.Course;
import com.dev.thesis_management.organization.dto.ProgramResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentResponse {
    UUID id;
    String fullName;
    String studentCode;
    LocalDate dob;
    String gender;
    String email;
    String phone;
    String address;
    ProgramResponse program;
    Course course;
    String avatarUrl;
}
