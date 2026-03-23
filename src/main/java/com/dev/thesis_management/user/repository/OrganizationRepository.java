package com.dev.thesis_management.user.repository;

import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findByIdOrCode(UUID id, String code);

    Optional<Organization> findByManager(User user);

    boolean existsByCodeAndId(String code, UUID id);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
}
