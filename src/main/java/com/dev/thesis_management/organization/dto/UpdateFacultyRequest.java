package com.dev.thesis_management.organization.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateFacultyRequest {
    String code;
    String name;
    String description;
}
