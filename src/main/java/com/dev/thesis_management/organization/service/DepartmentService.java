package com.dev.thesis_management.organization.service;

import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.exception.UnauthorizedException;
import com.dev.thesis_management.organization.dto.AddDepartmentRequest;
import com.dev.thesis_management.organization.dto.DepartmentResponse;
import com.dev.thesis_management.organization.dto.UpdateDepartmentRequest;
import com.dev.thesis_management.organization.entity.Department;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.mapper.DepartmentMapper;
import com.dev.thesis_management.organization.repository.DepartmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.organization.mapper.DepartmentMapper.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentService {
    DepartmentRepository departmentRepository;
    OrgService orgService;

    public DepartmentResponse addDepartment(AddDepartmentRequest request, UUID userId) {
        Organization org = orgService.findByUserId(userId);
        Department department = addDepartmentRequestToDepartment(request);
        if (departmentRepository.existsByCodeAndOrganization(request.getCode(), org)) {
            throw new BadRequestException("Department code already exists in organization");
        }
        department.setOrganization(org);
        return toDepartmentResponse(departmentRepository.save(department));
    }

    public DepartmentResponse updateDepartment(UUID departmentId, UpdateDepartmentRequest request, UUID userId){
        Department department = findDepartmentById(departmentId);
        if(!hasAuthorized(department, userId)){
           throw new UnauthorizedException("You do not have permission");
        }
        updateDepartmentFromRequest(department, request);
        return toDepartmentResponse(departmentRepository.save(department));
    }

    public void deleteDepartment(UUID departmentId, UUID userId) {
        Department department = findDepartmentById(departmentId);
        if(!hasAuthorized(department, userId)){
            throw new UnauthorizedException("You do not have permission");
        }
        departmentRepository.delete(department);
    }

    public boolean hasAuthorized(Department department, UUID userId){
        if(department.getOrganization() != null){
            return department.getOrganization().getManager().getId().equals(userId);
        }
        return true;
    }

    public Department findDepartmentById(UUID departmentId){
        return departmentRepository.findById(departmentId).orElseThrow();
    }

    public List<Department> findAllDepartmentByUser(UUID userId){
        return departmentRepository.findByUserId(userId);
    }

    public List<DepartmentResponse> getDepartments(UUID userId) {
        Organization organization = orgService.findByUserId(userId);
        return departmentRepository.findAllByOrganization(organization)
                .stream().map(DepartmentMapper::toDepartmentResponse)
                .toList();
    }
}
