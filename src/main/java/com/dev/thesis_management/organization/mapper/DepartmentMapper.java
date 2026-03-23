package com.dev.thesis_management.organization.mapper;

import com.dev.thesis_management.organization.dto.AddDepartmentRequest;
import com.dev.thesis_management.organization.dto.DepartmentResponse;
import com.dev.thesis_management.organization.dto.UpdateDepartmentRequest;
import com.dev.thesis_management.organization.entity.Department;

public class DepartmentMapper {
    public static Department addDepartmentRequestToDepartment(AddDepartmentRequest request){
        return Department.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    public static DepartmentResponse toDepartmentResponse(Department department){
        return DepartmentResponse.builder()
                .id(department.getId())
                .code(department.getCode())
                .name(department.getName())
                .description(department.getDescription())
                .build();
    }

    public static void updateDepartmentFromRequest(Department department, UpdateDepartmentRequest request){
        department.setCode(request.getCode());
        department.setName(request.getName());
        department.setDescription(request.getDescription());
    }
}
