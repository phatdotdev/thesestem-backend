package com.dev.thesis_management.category.repository;

import com.dev.thesis_management.category.entity.Field;
import com.dev.thesis_management.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FieldRepository extends JpaRepository<Field, UUID> {
    public List<Field> findAllByOrganizationId(UUID id);
    public Optional<Field> findByIdAndOrganization(UUID id, Organization organization);
}
