package com.dev.thesis_management.organization.mapper;

import com.dev.thesis_management.organization.dto.ProgramResponse;
import com.dev.thesis_management.organization.dto.UpdateProgramRequest;
import com.dev.thesis_management.organization.entity.Program;

public class ProgramMapper {
    public static ProgramResponse programToResponse(Program program){
        return ProgramResponse.builder()
                .id(program.getId())
                .code(program.getCode())
                .name(program.getName())
                .degree(program.getDegree())
                .description(program.getDescription())
                .college(program.getCollege() != null
                        ? CollegeMapper.collegeToResponse(program.getCollege())
                        : null)
                .faculty(program.getFaculty() != null
                        ? FacultyMapper.facultyToResponse(program.getFaculty())
                        : null)
                .department(program.getDepartment() != null
                        ? DepartmentMapper.toDepartmentResponse(program.getDepartment())
                        : null)
                .build();
    }

    public static void updateProgramFromRequest(Program program, UpdateProgramRequest request){
        program.setCode(request.getCode());
        program.setName(request.getName());
        program.setDegree(request.getDegree());
        program.setDescription(request.getDescription());
    }
}
