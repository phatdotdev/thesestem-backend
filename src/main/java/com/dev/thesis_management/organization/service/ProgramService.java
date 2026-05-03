package com.dev.thesis_management.organization.service;

import com.dev.thesis_management.exception.UnauthorizedException;
import com.dev.thesis_management.organization.dto.AddProgramRequest;
import com.dev.thesis_management.organization.dto.ProgramResponse;
import com.dev.thesis_management.organization.dto.UpdateProgramRequest;
import com.dev.thesis_management.organization.dto.organization.ProgramSearchForm;
import com.dev.thesis_management.organization.entity.Department;
import com.dev.thesis_management.organization.entity.Faculty;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.entity.Program;
import com.dev.thesis_management.organization.enums.ProgramManagedType;
import com.dev.thesis_management.organization.mapper.ProgramMapper;
import com.dev.thesis_management.organization.repository.ProgramRepository;
import com.dev.thesis_management.specifications.ProgramSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.organization.mapper.ProgramMapper.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProgramService {
    FacultyService facultyService;
    DepartmentService departmentService;
    OrgService orgService;

    ProgramRepository programRepository;

    public List<ProgramResponse> getPrograms(ProgramSearchForm form, UUID userId
    ){
        Organization organization = orgService.findByUserId(userId);
        return programRepository
                .findAll(ProgramSpecification.filterPrograms(form, organization.getId()))
                .stream()
                .map(ProgramMapper::programToResponse).toList();
    }

    public ProgramResponse addProgram(AddProgramRequest request, UUID userId){
        Organization organization = orgService.findByUserId(userId);
        Program program = Program.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .organization(organization)
                .degree(request.getDegree())
                .build();

        if(request.getManagedType().equals(ProgramManagedType.FACULTY)){
            if (request.getFacultyId() == null) {
                throw new IllegalArgumentException("FacultyId is required");
            }
            Faculty faculty = facultyService.findByFacultyId(request.getFacultyId());
            if(!facultyService.hasAuthorized(faculty, userId)){
                throw new UnauthorizedException("");
            }
            program.setFaculty(faculty);
        }
        if(request.getManagedType().equals(ProgramManagedType.DEPARTMENT)){
            if (request.getDepartmentId() == null) {
                throw new IllegalArgumentException("DepartmentId is required");
            }
            Department department = departmentService.findDepartmentById(request.getDepartmentId());
            if(!departmentService.hasAuthorized(department, userId)){
                throw new UnauthorizedException("");
            }
            program.setDepartment(department);
        }
        return programToResponse(programRepository.save(program));
    }

    public ProgramResponse updateProgram(UpdateProgramRequest request, UUID programId, UUID userId){
        Program program = findProgramById(programId);
        if(!hasAuthorized(program, userId)){
            throw new UnauthorizedException("");
        }
        updateProgramFromRequest(program, request);
        if(request.getManagedType().equals(ProgramManagedType.FACULTY)){
            if (request.getFacultyId() == null) {
                throw new IllegalArgumentException("FacultyId is required");
            }
            Faculty faculty = facultyService.findByFacultyId(request.getFacultyId());
            if(!facultyService.hasAuthorized(faculty, userId)){
                throw new UnauthorizedException("");
            }
            program.setFaculty(faculty);
        }
        if(request.getManagedType().equals(ProgramManagedType.DEPARTMENT)){
            if (request.getDepartmentId() == null) {
                throw new IllegalArgumentException("DepartmentId is required");
            }
            Department department = departmentService.findDepartmentById(request.getDepartmentId());
            if(!departmentService.hasAuthorized(department, userId)){
                throw new UnauthorizedException("");
            }
            program.setDepartment(department);
        }
        return programToResponse(programRepository.save(program));
    }

    public void deleteProgram(UUID programId, UUID userId){
        Program program = findProgramById(programId);
        if(!hasAuthorized(program, userId)){
            throw new UnauthorizedException("");
        }
        programRepository.delete(program);
    }

    public boolean hasAuthorized(Program program, UUID userId){
        if(program.getFaculty() != null){
            return facultyService.hasAuthorized(program.getFaculty(), userId);
        }
        if(program.getDepartment() != null){
            return departmentService.hasAuthorized(program.getDepartment(), userId);
        }
        return true;
    }

    public Program findProgramById(UUID id){
        return programRepository.findById(id).orElseThrow();
    }
}
