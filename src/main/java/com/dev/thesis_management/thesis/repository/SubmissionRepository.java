package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.ThesisSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<ThesisSubmission, UUID> {
    Optional<Integer> findMaxVersionByThesisId(UUID id);
}
