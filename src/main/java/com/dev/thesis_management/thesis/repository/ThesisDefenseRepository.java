package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.ThesisDefense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ThesisDefenseRepository extends JpaRepository<ThesisDefense, UUID>, JpaSpecificationExecutor<ThesisDefense> {
    boolean existsByThesisId(UUID thesisId);

    @Query("""
        select d
        from ThesisDefense d
        join d.thesis t
        join t.topic tp
        join tp.group g
        join g.semester s
        where s.id = :semesterId
    """)
    Page<ThesisDefense> findBySemester(UUID semesterId, Pageable pageable);

    List<ThesisDefense> findAllByCouncilId(UUID id);

    Optional<ThesisDefense> findAllByThesisId(UUID id);
}