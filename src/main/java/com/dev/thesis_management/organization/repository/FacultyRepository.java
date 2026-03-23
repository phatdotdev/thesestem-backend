package com.dev.thesis_management.organization.repository;

import com.dev.thesis_management.organization.entity.Faculty;
import com.dev.thesis_management.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FacultyRepository extends JpaRepository<Faculty, UUID> {
    boolean existsByCodeAndOrganization(String code, Organization org);

    @Query("""
        SELECT DISTINCT f
        FROM Faculty f
        LEFT JOIN f.college c
        LEFT JOIN c.organization co
        LEFT JOIN f.organization fo
        LEFT JOIN co.manager u1
        LEFT JOIN fo.manager u2
        WHERE u1.id = :userId OR u2.id = :userId
        """)
    public List<Faculty> findByUserId(@Param("userId") UUID userId);

    List<Faculty> findAllByOrganization(Organization org);

    Optional<Faculty> findByIdAndOrganization(UUID uuid, Organization organization);
}
