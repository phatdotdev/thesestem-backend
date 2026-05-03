package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.ThesisDefense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ThesisDefenseRepository extends JpaRepository<ThesisDefense, UUID>, JpaSpecificationExecutor<ThesisDefense> {
    boolean existsByThesisId(UUID thesisId);

    long countByThesis_Topic_Group_Semester_Id(UUID semesterId);

    long countByThesis_Topic_Group_Semester_IdAndStatus(UUID semesterId, ThesisDefense.Status status);

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

    @Query("""
        select distinct d
        from ThesisDefense d
        join fetch d.thesis t
        join fetch t.topic tp
        join fetch tp.group g
        left join fetch d.council c
        left join fetch c.members cm
        left join fetch cm.lecturer
        left join fetch cm.role
        left join fetch d.scores sc
        left join fetch sc.councilMember scm
        left join fetch scm.lecturer
        left join fetch scm.role
        where g.semester.id = :semesterId
    """)
    List<ThesisDefense> findDetailedBySemesterId(@Param("semesterId") UUID semesterId);

    @Query("""
            select count(distinct d.id)
            from ThesisDefense d
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
    long countByScope(
            @Param("organizationId") UUID organizationId,
            @Param("semesterId") UUID semesterId,
            @Param("collegeId") UUID collegeId,
            @Param("facultyId") UUID facultyId,
            @Param("departmentId") UUID departmentId
    );

    @Query("""
            select count(distinct d.council.id)
            from ThesisDefense d
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
    long countDistinctCouncilsByScope(
            @Param("organizationId") UUID organizationId,
            @Param("semesterId") UUID semesterId,
            @Param("collegeId") UUID collegeId,
            @Param("facultyId") UUID facultyId,
            @Param("departmentId") UUID departmentId
    );
}