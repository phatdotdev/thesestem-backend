package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.DefenseScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DefenseScoreRepository extends JpaRepository<DefenseScore, UUID> {
    Optional<DefenseScore> findByDefenseIdAndCouncilMemberId(UUID defenseId, UUID councilMemberId);

    @Query("""
            select avg(ds.score)
            from DefenseScore ds
            where ds.defense.thesis.topic.group.mentor.id = :mentorId
            """)
    Double findAverageThesisScoreByMentorId(@Param("mentorId") UUID mentorId);

    @Query("""
            select avg(ds.score)
            from DefenseScore ds
            where ds.defense.thesis.topic.group.mentor.id = :mentorId
              and ds.defense.thesis.topic.group.semester.id = :semesterId
            """)
    Double findAverageThesisScoreByMentorIdAndSemesterId(
            @Param("mentorId") UUID mentorId,
            @Param("semesterId") UUID semesterId
    );

    @Query("""
            select avg(ds.score)
            from DefenseScore ds
            where ds.councilMember.lecturer.id = :lecturerId
            """)
    Double findAverageGivenScoreByLecturerId(@Param("lecturerId") UUID lecturerId);

    @Query("""
            select avg(ds.score)
            from DefenseScore ds
            where ds.councilMember.lecturer.id = :lecturerId
              and ds.defense.thesis.topic.group.semester.id = :semesterId
            """)
    Double findAverageGivenScoreByLecturerIdAndSemesterId(
            @Param("lecturerId") UUID lecturerId,
            @Param("semesterId") UUID semesterId
    );

    @Query("""
            select avg(ds.score)
            from DefenseScore ds
            join ds.defense d
            join d.thesis t
            join t.student s
            left join s.program p
            left join p.department dep
            left join p.faculty fac
            left join p.college col
            left join dep.faculty depFac
            left join fac.college facCol
            where s.organization.id = :organizationId
              and (:semesterId is null or t.topic.group.semester.id = :semesterId)
              and (:departmentId is null or dep.id = :departmentId)
              and (:facultyId is null or fac.id = :facultyId or depFac.id = :facultyId)
              and (:collegeId is null or col.id = :collegeId or facCol.id = :collegeId or depFac.college.id = :collegeId)
            """)
    Double findAverageThesisScoreByScope(
            @Param("organizationId") UUID organizationId,
            @Param("semesterId") UUID semesterId,
            @Param("collegeId") UUID collegeId,
            @Param("facultyId") UUID facultyId,
            @Param("departmentId") UUID departmentId
    );
}
