package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.Council;
import com.dev.thesis_management.thesis.entity.CouncilMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CouncilMemberRepository extends JpaRepository<CouncilMember, UUID> {
        @Query("""
        SELECT cm.council
        FROM CouncilMember cm
        WHERE cm.lecturer.id = :lecturerId
    """)
        List<Council> findCouncilsByLecturerId(UUID lecturerId);

    @Query("""
        SELECT cm.council
        FROM CouncilMember cm
        WHERE cm.lecturer.id = :lecturerId
        AND cm.council.semester.id = :semesterId
    """)
    List<Council> findCouncilsByLecturerIdAndSemesterId(
            UUID lecturerId,
            UUID semesterId
    );
}

