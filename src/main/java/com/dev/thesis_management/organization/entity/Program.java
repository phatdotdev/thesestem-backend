package com.dev.thesis_management.organization.entity;

import com.dev.thesis_management.user.entity.Student;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "programs",
        uniqueConstraints = {},
        indexes = {
                @Index(name = "idx_program_faculty", columnList = "faculty_id"),
                @Index(name = "idx_program_department", columnList = "department_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Program {
    @Id
    @GeneratedValue
    UUID id;

    @Column(nullable = false)
    String code;

    @Column(nullable = false)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(nullable = false)
    String degree; // BACHELOR, ENGINEERING

    @ManyToOne
    @JoinColumn(name = "college_id")
    College college;

    @ManyToOne
    @JoinColumn(name = "faculty_id")
    Faculty faculty;

    @ManyToOne
    @JoinColumn(name = "department_id")
    Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    Organization organization;

    /* audit */
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
