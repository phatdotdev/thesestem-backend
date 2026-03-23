package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.DefenseScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DefenseScoreRepository extends JpaRepository<DefenseScore, UUID> {
    Optional<DefenseScore> findByDefenseIdAndCouncilMemberId(UUID defenseId, UUID councilMemberId);
}
