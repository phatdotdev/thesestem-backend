package com.dev.thesis_management.organization.repository;

import com.dev.thesis_management.organization.entity.College;
import com.dev.thesis_management.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollegeRepository extends JpaRepository<College, UUID> {
    Optional<College> findByIdAndOrganization(UUID id, Organization organization);

    List<College> findAllByOrganization(Organization organization);
}
