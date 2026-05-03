package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.Milestone;
import com.dev.thesis_management.thesis.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {
    List<Milestone> findBySemester(Semester semester);

    Optional<Milestone> findByIdAndSemester(UUID id, Semester semester);


}
