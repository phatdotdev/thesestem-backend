package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.Thesis;
import com.dev.thesis_management.user.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ThesisRepository extends JpaRepository<Thesis, UUID>, JpaSpecificationExecutor<Thesis> {
    List<Thesis> findAllByTopic_Group_Id(UUID groupId);
    List<Thesis> findAllByTopic_Group_Semester_IdAndStudent(UUID semesterId, Student student);

    List<Thesis> findByTopic_Group_Id(UUID groupId);
    long countByTopic_Group_Semester_Id(UUID semesterId);
    long countByTopic_Group_Semester_IdAndStatus(UUID semesterId, Thesis.Status status);
    long countByAccessLevel(Thesis.AccessLevel accessLevel);

    @Query("""
            select distinct t
            from Thesis t
            join fetch t.topic tp
            join fetch tp.group g
            left join fetch g.mentor
            join fetch t.student
            where g.semester.id = :semesterId
            """)
    List<Thesis> findDetailedBySemesterId(@Param("semesterId") UUID semesterId);

    Optional<Thesis> findByIdAndAccessLevel(UUID id, Thesis.AccessLevel accessLevel);

    long countByTopic_Group_Semester_IdAndTopic_Group_Mentor_Id(UUID semesterId, UUID mentorId);

    long countByTopic_Group_Mentor_Id(UUID mentorId);

    @Query("""
            select count(distinct t.id)
            from Thesis t
            join t.student s
            left join s.program p
            left join p.department d
            left join p.faculty f
            left join p.college c
            left join d.faculty df
            left join f.college fc
            where s.organization.id = :organizationId
              and (:semesterId is null or t.topic.group.semester.id = :semesterId)
              and (:departmentId is null or d.id = :departmentId)
              and (:facultyId is null or f.id = :facultyId or df.id = :facultyId)
              and (:collegeId is null or c.id = :collegeId or fc.id = :collegeId or df.college.id = :collegeId)
            """)
    long countByScope(
            @Param("organizationId") UUID organizationId,
            @Param("semesterId") UUID semesterId,
            @Param("collegeId") UUID collegeId,
            @Param("facultyId") UUID facultyId,
            @Param("departmentId") UUID departmentId
    );

    @Query("""
            select count(distinct t.id)
            from Thesis t
            join t.student s
            left join s.program p
            left join p.department d
            left join p.faculty f
            left join p.college c
            left join d.faculty df
            left join f.college fc
            where s.organization.id = :organizationId
              and (:semesterId is null or t.topic.group.semester.id = :semesterId)
              and (:departmentId is null or d.id = :departmentId)
              and (:facultyId is null or f.id = :facultyId or df.id = :facultyId)
              and (:collegeId is null or c.id = :collegeId or fc.id = :collegeId or df.college.id = :collegeId)
              and t.status = :status
            """)
    long countByScopeAndStatus(
            @Param("organizationId") UUID organizationId,
            @Param("semesterId") UUID semesterId,
            @Param("collegeId") UUID collegeId,
            @Param("facultyId") UUID facultyId,
            @Param("departmentId") UUID departmentId,
            @Param("status") Thesis.Status status
    );

    @Query("""
            select count(distinct t.id)
            from Thesis t
            join t.student s
            left join s.program p
            left join p.department d
            left join p.faculty f
            left join p.college c
            left join d.faculty df
            left join f.college fc
            where s.organization.id = :organizationId
              and (:semesterId is null or t.topic.group.semester.id = :semesterId)
              and (:departmentId is null or d.id = :departmentId)
              and (:facultyId is null or f.id = :facultyId or df.id = :facultyId)
              and (:collegeId is null or c.id = :collegeId or fc.id = :collegeId or df.college.id = :collegeId)
              and t.accessLevel = :accessLevel
            """)
    long countByScopeAndAccessLevel(
            @Param("organizationId") UUID organizationId,
            @Param("semesterId") UUID semesterId,
            @Param("collegeId") UUID collegeId,
            @Param("facultyId") UUID facultyId,
            @Param("departmentId") UUID departmentId,
            @Param("accessLevel") Thesis.AccessLevel accessLevel
    );

    Optional<Thesis> findByTopic_Group_IdAndStudent_Id(UUID id, UUID studentId);
}
