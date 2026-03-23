package com.dev.thesis_management.thesis.mapper;

import com.dev.thesis_management.thesis.dto.CreateSemesterRequest;
import com.dev.thesis_management.thesis.dto.SemesterResponse;
import com.dev.thesis_management.thesis.dto.UpdateSemesterRequest;
import com.dev.thesis_management.thesis.entity.Semester;

public class SemesterMapper {

    public static Semester createSemesterToSemester(CreateSemesterRequest request){
        return Semester.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
    }

    public static void updateSemesterFromRequest(Semester semester, UpdateSemesterRequest request){
        semester.setName(request.name());
        semester.setStartDate(semester.getStartDate());
        semester.setEndDate(semester.getEndDate());
    }

    public static SemesterResponse semesterToResponse(Semester semester){
        return SemesterResponse.builder()
                .id(semester.getId())
                .name(semester.getName())
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .status(semester.getStatus().name())
                .year(semester.getAcademicYear())
                .createdAt(semester.getCreatedAt())
                .updatedAt(semester.getCreatedAt())
                .build();
    }
}
