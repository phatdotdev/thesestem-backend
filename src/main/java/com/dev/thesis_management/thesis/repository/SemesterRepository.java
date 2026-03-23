package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.category.entity.AcademicYear;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.thesis.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {

    public List<Semester> findByOrganization(Organization organization);
    
    public Semester findByStatus(Semester.Status status);

    public Optional<Semester> findByIdAndOrganization(UUID id, Organization org);

    public Optional<Semester> findByOrganizationAndStatus(Organization organization, Semester.Status status);

    List<Semester> findAllByIdInAndOrganization(List<UUID> mentorIds, Organization org);

    List<Semester> findAllByOrganizationAndAcademicYear(Organization org, AcademicYear year);
}
