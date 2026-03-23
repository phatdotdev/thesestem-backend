package com.dev.thesis_management.user.dto;

import com.dev.thesis_management.organization.dto.CollegeResponse;
import com.dev.thesis_management.organization.dto.DepartmentResponse;
import com.dev.thesis_management.organization.dto.FacultyResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LecturerResponse {
    UUID id;
    String fullName;
    String lecturerCode;
    String email;
    String gender;
    LocalDate dob;
    String phone;
    String address;
    String avatarUrl;
    CollegeResponse college;
    FacultyResponse faculty;
    DepartmentResponse department;
}
