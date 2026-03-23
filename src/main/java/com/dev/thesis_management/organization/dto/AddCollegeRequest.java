package com.dev.thesis_management.organization.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddCollegeRequest {
    String code;
    String name;
    String description;
}
