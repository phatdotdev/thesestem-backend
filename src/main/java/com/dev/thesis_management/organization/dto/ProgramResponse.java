package com.dev.thesis_management.organization.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProgramResponse {
    UUID id;
    String code;
    String name;
    String degree;

    CollegeResponse college;
    FacultyResponse faculty;
    DepartmentResponse department;

    String description;
}
