package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.thesis.entity.CouncilRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouncilRoleRepository extends JpaRepository<CouncilRole, UUID> {
    List<CouncilRole> findByOrganization(Organization org);

    Optional<CouncilRole> findByIdAndOrganization(UUID uuid, Organization org);

    List<CouncilRole> findAllByOrganization(Organization organization);
}
