package com.dev.thesis_management.user.repository;

import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.user.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LecturerRepository extends JpaRepository<Lecturer, UUID>, JpaSpecificationExecutor<Lecturer> {
    Optional<Lecturer> findByOrganizationAndLecturerCode(
            Organization organization,
            String lecturerCode
    );

    Optional<Lecturer> findByIdAndOrganization(UUID id, Organization org);

    List<Lecturer> findAllByIdInAndOrganization(List<UUID> mentorIds, Organization org);

    List<Lecturer> findAllByOrganization(Organization organization);

    boolean existsByLecturerCodeAndOrganization(String code, Organization organization);

    boolean existsByEmailAndOrganization(String email, Organization organization);
}
