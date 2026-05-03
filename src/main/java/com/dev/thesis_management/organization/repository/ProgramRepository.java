package com.dev.thesis_management.organization.repository;

import com.dev.thesis_management.organization.entity.Department;
import com.dev.thesis_management.organization.entity.Faculty;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.entity.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProgramRepository extends JpaRepository<Program, UUID>, JpaSpecificationExecutor<Program> {
    Page<Program> findByFaculty_Id(UUID facultyId, Pageable pageable);

    Page<Program> findByDepartment_Id(UUID departmentId, Pageable pageable);

    Page<Program> findByFacultyInOrDepartmentIn(
            List<Faculty> faculties,
            List<Department> departments,
            Pageable pageable
    );

    Optional<Program> findByIdAndOrganization(UUID id, Organization organization);

    List<Program> findAllByOrganization(Organization organization);
}
