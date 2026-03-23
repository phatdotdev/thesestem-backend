package com.dev.thesis_management.user.repository;

import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.user.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {
    Optional<Student> findByOrganizationAndStudentCode(
            Organization organization,
            String studentCode
    );

    Optional<Student> findByIdAndOrganization(UUID stdId, Organization org);

    List<Student> findAllByIdInAndOrganization(List<UUID> stdIds, Organization org);

    List<Student> findAllByOrganization(Organization organization);
}
