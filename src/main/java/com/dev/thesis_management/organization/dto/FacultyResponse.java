package com.dev.thesis_management.organization.dto;

import com.dev.thesis_management.organization.entity.Department;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FacultyResponse {
    UUID id;
    String code;
    String name;
    String description;
    List<DepartmentResponse> departments;
}
