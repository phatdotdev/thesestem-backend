package com.dev.thesis_management.organization.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "faculties",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_faculty_college_code",
                        columnNames = {"college_id", "code"}
                ),
                @UniqueConstraint(
                        name = "uk_faculty_org_code",
                        columnNames = {"organization_id", "code"}
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Faculty {

    @Id
    @GeneratedValue
    UUID id;

    @Column(nullable = false)
    String code;

    String name;

    @Column(columnDefinition = "TEXT")
    String description;


    /* relations */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "organization_id",
            foreignKey = @ForeignKey(name = "fk_faculty_org")
    )
    Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "college_id",
            foreignKey = @ForeignKey(name = "fk_faculty_college")
    )
    College college;

    @OneToMany(
            mappedBy = "faculty",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<Department> departments = new ArrayList<>();

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

    private void validateParent(){
        if (college == null && organization == null) {
            throw new IllegalStateException("Faculty must belong to a college or an organization");
        }
        if (college != null && organization != null) {
            throw new IllegalStateException("Faculty cannot belong to both college and organization");
        }
    }
}
