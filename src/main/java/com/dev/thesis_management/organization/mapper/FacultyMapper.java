package com.dev.thesis_management.organization.mapper;

import com.dev.thesis_management.organization.dto.AddFacultyRequest;
import com.dev.thesis_management.organization.dto.FacultyResponse;
import com.dev.thesis_management.organization.dto.UnitResponse;
import com.dev.thesis_management.organization.dto.UpdateFacultyRequest;
import com.dev.thesis_management.organization.entity.Faculty;

import com.dev.thesis_management.organization.mapper.DepartmentMapper.*;

public class FacultyMapper {
    public static Faculty addFacultyRequestToFaculty(AddFacultyRequest request){
        return Faculty.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    public static FacultyResponse facultyToResponse(Faculty faculty){
        return FacultyResponse.builder()
                .id(faculty.getId())
                .code(faculty.getCode())
                .name(faculty.getName())
                .description(faculty.getDescription())
                .departments(faculty.getDepartments() != null
                        ? faculty.getDepartments().stream().map(DepartmentMapper::toDepartmentResponse).toList()
                        : null)
                .build();
    }

    public static void updateFacultyFromRequest(Faculty faculty, UpdateFacultyRequest request){
        faculty.setCode(request.getCode());
        faculty.setName(request.getName());
        faculty.setDescription(request.getDescription());
    }

    public static FacultyResponse facultyToResponseWithoutChildren(Faculty faculty) {
        return FacultyResponse.builder()
                .id(faculty.getId())
                .code(faculty.getCode())
                .name(faculty.getName())
                .description(faculty.getDescription())
                .college(faculty.getCollege() != null ? UnitResponse.builder()
                        .id(faculty.getCollege().getId())
                        .code(faculty.getCollege().getCode())
                        .name(faculty.getCollege().getName())
                        .build() : null)
                .build();
    }
}
