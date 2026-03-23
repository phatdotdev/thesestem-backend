package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    Optional<Assignment> findByIdAndGroupId(UUID assignmentId, UUID groupId);
}
