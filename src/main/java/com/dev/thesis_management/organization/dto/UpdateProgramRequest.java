package com.dev.thesis_management.organization.dto;

import com.dev.thesis_management.organization.enums.ProgramManagedType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProgramRequest {
    String code;
    String name;
    String description;
    String degree;
    ProgramManagedType managedType;
    UUID departmentId;
    UUID facultyId;
}
