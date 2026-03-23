package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.Council;
import com.dev.thesis_management.thesis.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CouncilRepository extends JpaRepository<Council, UUID>, JpaSpecificationExecutor<Council> {
    Optional<Council> findByIdAndSemester(UUID id, Semester semester);
}
