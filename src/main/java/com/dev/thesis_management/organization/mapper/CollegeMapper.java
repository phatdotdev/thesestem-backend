package com.dev.thesis_management.organization.mapper;

import com.dev.thesis_management.organization.dto.AddCollegeRequest;
import com.dev.thesis_management.organization.dto.CollegeResponse;
import com.dev.thesis_management.organization.dto.UpdateCollegeRequest;
import com.dev.thesis_management.organization.entity.College;

public class CollegeMapper {
    public static College addCollegeRequestToCollege(AddCollegeRequest request){
        return College.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    public static CollegeResponse collegeToResponse(College college){
        return CollegeResponse.builder()
                .id(college.getId())
                .code(college.getCode())
                .name(college.getName())
                .description(college.getDescription())
                .faculties(
                        college.getFaculties() != null ?
                        college.getFaculties()
                        .stream().map(FacultyMapper::facultyToResponse)
                        .toList() : null)
                .build();
    }

    public static void updateCollegeFromRequest(College college, UpdateCollegeRequest request){
        college.setCode(request.getCode());
        college.setName(request.getName());
        college.setDescription(request.getDescription());
    }
}
