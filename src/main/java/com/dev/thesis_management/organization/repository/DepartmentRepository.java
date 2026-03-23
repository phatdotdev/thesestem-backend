package com.dev.thesis_management.organization.repository;

import com.dev.thesis_management.organization.entity.Department;
import com.dev.thesis_management.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    @Query("""
        SELECT DISTINCT d
        FROM Department d
        JOIN d.faculty f
        LEFT JOIN f.college c
        LEFT JOIN c.organization co
        LEFT JOIN f.organization fo
        LEFT JOIN co.manager u1
        LEFT JOIN fo.manager u2
        WHERE u1.id = :userId OR u2.id = :userId
    """)
    List<Department> findByUserId(@Param("userId") UUID userId);


    List<Department> findAllByOrganization(Organization organization);

    Optional<Department> findByIdAndOrganization(UUID uuid, Organization organization);

    boolean existsByCodeAndOrganization(String code, Organization org);
}
