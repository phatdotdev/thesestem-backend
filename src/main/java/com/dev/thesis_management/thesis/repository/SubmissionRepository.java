package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.ThesisSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<ThesisSubmission, UUID> {
    @Query("""
        SELECT MAX(s.version)
        FROM ThesisSubmission s
        WHERE s.thesis.id = :id
       """)
    Optional<Integer> findMaxVersionByThesisId(UUID id);
}
