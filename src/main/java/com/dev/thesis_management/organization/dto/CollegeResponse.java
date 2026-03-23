package com.dev.thesis_management.organization.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CollegeResponse {
    UUID id;
    String code;
    String name;
    String description;
    List<FacultyResponse> faculties;
}
