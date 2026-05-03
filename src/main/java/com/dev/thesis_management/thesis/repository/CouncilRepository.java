package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.Council;
import com.dev.thesis_management.thesis.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CouncilRepository extends JpaRepository<Council, UUID>, JpaSpecificationExecutor<Council> {
    Optional<Council> findByIdAndSemester(UUID id, Semester semester);

    long countBySemester_Id(UUID semesterId);

    @Query("""
    select count(distinct c.id)
    from Council c
    left join c.department d
    left join c.faculty f
    left join c.college col
    left join d.faculty df
    left join f.college fc

    where
        c.semester.organization.id = :organizationId

        and (:semesterId is null or c.semester.id = :semesterId)

        and (
            (:collegeId is null and :facultyId is null and :departmentId is null)
            or (
                (:departmentId is null or d.id = :departmentId)

                and (
                    :facultyId is null
                    or f.id = :facultyId
                    or df.id = :facultyId
                )

                and (
                    :collegeId is null
                    or col.id = :collegeId
                    or fc.id = :collegeId
                    or df.college.id = :collegeId
                )
            )
        )
""")
    long countCouncils(
            UUID organizationId,
            UUID semesterId,
            UUID departmentId,
            UUID facultyId,
            UUID collegeId
    );
}
