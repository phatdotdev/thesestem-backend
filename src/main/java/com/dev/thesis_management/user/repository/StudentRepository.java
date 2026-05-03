package com.dev.thesis_management.user.repository;

import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.user.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {
    Optional<Student> findByOrganizationAndStudentCode(
            Organization organization,
            String studentCode
    );

    Optional<Student> findByIdAndOrganization(UUID stdId, Organization org);

    List<Student> findAllByIdInAndOrganization(List<UUID> stdIds, Organization org);

    List<Student> findAllByOrganization(Organization organization);

    @Query("""
            select count(distinct s.id)
            from Student s
            left join s.program p
            left join p.department d
            left join p.faculty f
            left join p.college c
            left join d.faculty df
            left join f.college fc
            where s.organization.id = :organizationId
              and (
                    :semesterId is null
                    or exists (
                        select 1
                        from Semester sem
                        join sem.students semStudent
                        where sem.id = :semesterId
                          and semStudent.id = s.id
                    )
              )
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
}
