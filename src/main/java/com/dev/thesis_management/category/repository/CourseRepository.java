package com.dev.thesis_management.category.repository;

import com.dev.thesis_management.category.entity.Course;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.user.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findAllByOrganizationId(UUID id);

    Optional<Course> findByIdAndOrganization(UUID uuid, Organization organization);
}
