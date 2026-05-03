package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.Group;
import com.dev.thesis_management.thesis.entity.Semester;
import com.dev.thesis_management.user.entity.Lecturer;
import com.dev.thesis_management.user.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findAllBySemesterAndMentor(Semester semester, Lecturer mentor);

    List<Group> findAllBySemesterAndStudentsContains(Semester semester, Student student);

    long countBySemester_Id(UUID semesterId);
    long countBySemester_IdAndMentor_Id(UUID semesterId, UUID mentorId);
    long countByMentor_Id(UUID mentorId);

    @Query("""
            select count(distinct g.id)
            from Group g
            left join g.mentor m
            left join m.department d
            left join m.faculty f
            left join m.college c
            left join d.faculty df
            left join f.college fc
            where g.semester.id = :semesterId
              and g.semester.organization.id = :organizationId
              and (
                    (:collegeId is null and :facultyId is null and :departmentId is null)
                    or (
                        m is not null
                        and m.organization.id = :organizationId
                        and (:departmentId is null or d.id = :departmentId)
                        and (:facultyId is null or f.id = :facultyId or df.id = :facultyId)
                        and (:collegeId is null or c.id = :collegeId or fc.id = :collegeId or df.college.id = :collegeId)
                    )
              )
            """)
    long countBySemesterAndMentorScope(
            @Param("semesterId") UUID semesterId,
            @Param("organizationId") UUID organizationId,
            @Param("collegeId") UUID collegeId,
            @Param("facultyId") UUID facultyId,
            @Param("departmentId") UUID departmentId
    );

    @Query("""
            select count(distinct m.id)
            from Group g
            join g.mentor m
            left join m.department d
            left join m.faculty f
            left join m.college c
            left join d.faculty df
            left join f.college fc
            where g.semester.id = :semesterId
              and m.organization.id = :organizationId
              and (:departmentId is null or d.id = :departmentId)
              and (:facultyId is null or f.id = :facultyId or df.id = :facultyId)
              and (:collegeId is null or c.id = :collegeId or fc.id = :collegeId or df.college.id = :collegeId)
            """)
    long countDistinctMentorBySemesterAndScope(
            @Param("semesterId") UUID semesterId,
            @Param("organizationId") UUID organizationId,
            @Param("collegeId") UUID collegeId,
            @Param("facultyId") UUID facultyId,
            @Param("departmentId") UUID departmentId
    );
}
