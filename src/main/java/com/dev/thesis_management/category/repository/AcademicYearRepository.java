package com.dev.thesis_management.category.repository;

import com.dev.thesis_management.category.entity.AcademicYear;
import com.dev.thesis_management.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {
    List<AcademicYear> findByOrganizationId(UUID id);

    Optional<AcademicYear> findByIdAndOrganization(UUID id, Organization organization);
}
