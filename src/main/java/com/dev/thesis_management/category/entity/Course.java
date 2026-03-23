package com.dev.thesis_management.category.entity;

import com.dev.thesis_management.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "courses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_course_org_code",
                        columnNames = {"organization_id", "code"}
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Course {

    /* id */
    @Id
    @GeneratedValue
    UUID id;

    /* organization */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    Organization organization;

    /* course info */
    @Column(nullable = false)
    String code;

    @Column(nullable = false)
    String name;

    Integer startYear;
    Integer endYear;

    LocalDate startDate;
    LocalDate endDate;

    Boolean active = true;

    /* audit */
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (active == null) active = true;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
