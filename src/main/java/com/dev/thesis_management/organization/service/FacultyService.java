package com.dev.thesis_management.organization.service;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.exception.UnauthorizedException;
import com.dev.thesis_management.organization.dto.AddDepartmentRequest;
import com.dev.thesis_management.organization.dto.AddFacultyRequest;
import com.dev.thesis_management.organization.dto.FacultyResponse;
import com.dev.thesis_management.organization.dto.UpdateFacultyRequest;
import com.dev.thesis_management.organization.entity.Department;
import com.dev.thesis_management.organization.entity.Faculty;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.mapper.FacultyMapper;
import com.dev.thesis_management.organization.repository.DepartmentRepository;
import com.dev.thesis_management.organization.repository.FacultyRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.organization.mapper.FacultyMapper.addFacultyRequestToFaculty;
import static com.dev.thesis_management.organization.mapper.FacultyMapper.*;
import static com.dev.thesis_management.organization.mapper.DepartmentMapper.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FacultyService {

    FacultyRepository facultyRepository;
    DepartmentRepository departmentRepository;

    OrgService orgService;

    @Transactional
    public FacultyResponse addFaculty(AddFacultyRequest request, UUID userId){

        Organization org = orgService.findByUserId(userId);

        if (facultyRepository.existsByCodeAndOrganization(
                request.getCode(), org)) {
            throw new BadRequestException("Faculty code already exists in organization");
        }

        Faculty faculty = addFacultyRequestToFaculty(request);
        faculty.setOrganization(org);
        faculty.setCollege(null);

        facultyRepository.save(faculty);
        org.getFaculties().add(faculty);

        return facultyToResponse(faculty);
    }

    public FacultyResponse updateFaculty(UUID facultyId, UpdateFacultyRequest request, UUID userId) {
        Faculty faculty = findByFacultyId(facultyId);
        if(!hasAuthorized(faculty, userId)){
            throw new UnauthorizedException("You do not have permission");
        }
        updateFacultyFromRequest(faculty, request);
        return facultyToResponse(facultyRepository.save(faculty));
    }

    public void deleteFaculty(UUID facultyId, UUID userId){
        Faculty faculty = findByFacultyId(facultyId);
        if(!hasAuthorized(faculty, userId)){
            throw new UnauthorizedException("You do not hava permission");
        }
        facultyRepository.delete(faculty);
    }

    public FacultyResponse addDepartment(UUID facultyId, AddDepartmentRequest request, UUID userId){
        Organization org = orgService.findByUserId(userId);

        Faculty faculty = findByFacultyId(facultyId);
        if(!hasAuthorized(faculty, userId)){
            throw new UnauthorizedException("You do not have permission");
        }
        Department department = addDepartmentRequestToDepartment(request);
        department.setFaculty(faculty);
        department.setOrganization(org);
        departmentRepository.save(department);
        faculty.getDepartments().add(department);
        return facultyToResponse(faculty);
    }


    public Faculty findByFacultyId(UUID facultyId){
        return facultyRepository.findById(facultyId).orElseThrow();
    }

    public boolean hasAuthorized(Faculty faculty, UUID userId){
        if(faculty.getOrganization() != null){
            return faculty.getOrganization()
                    .getManager()
                    .getId()
                    .equals(userId);
        }

        if(faculty.getCollege() != null){
            return faculty.getCollege()
                    .getOrganization()
                    .getManager()
                    .getId()
                    .equals(userId);
        }

        return false;
    }

    public List<Faculty> findAllFacultiesByUser(UUID userId){
        return facultyRepository.findByUserId(userId);
    }

    public List<FacultyResponse> getFaculties(UUID userId) {
        Organization org = orgService.findByUserId(userId);
        return facultyRepository.findAllByOrganization(org).stream().map(FacultyMapper::facultyToResponseWithoutChildren).toList();
    }
}
