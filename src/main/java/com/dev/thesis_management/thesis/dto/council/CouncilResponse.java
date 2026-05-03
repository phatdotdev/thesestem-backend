package com.dev.thesis_management.thesis.dto.council;

import com.dev.thesis_management.organization.dto.CollegeResponse;
import com.dev.thesis_management.organization.dto.DepartmentResponse;
import com.dev.thesis_management.organization.dto.FacultyResponse;
import com.dev.thesis_management.organization.entity.Faculty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouncilResponse {
    UUID id;
    String name;
    String code;
    List<CouncilMemberResponse> members;
    CollegeResponse college;
    FacultyResponse faculty;
    DepartmentResponse department;
}
