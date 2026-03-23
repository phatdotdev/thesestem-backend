package com.dev.thesis_management.organization.entity;

import com.dev.thesis_management.category.entity.AcademicYear;
import com.dev.thesis_management.file_asset.entity.FileAsset;
import com.dev.thesis_management.thesis.entity.CouncilRole;
import com.dev.thesis_management.thesis.entity.Semester;
import com.dev.thesis_management.user.entity.Lecturer;
import com.dev.thesis_management.user.entity.Student;
import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.enums.OrganizationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "organizations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Organization {

    /* auth */
    @Id
    @GeneratedValue
    UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "manager_id", nullable = false, unique = true)
    User manager;

    String code;

    String name;

    String email;

    String website;

    String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    OrganizationType type;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(columnDefinition = "TEXT")
    String address;

    /* structure */
    @OneToMany(
            mappedBy = "organization",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<College> colleges = new ArrayList<>();

    @OneToMany(
            mappedBy = "organization",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<Faculty> faculties = new ArrayList<>();

    @OneToMany(
            mappedBy = "organization",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<Department> departments = new ArrayList<>();


    /* process */
    @OneToMany(
            mappedBy = "organization",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<Student> students = new ArrayList<>();

    @OneToMany(
            mappedBy = "organization",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<Lecturer> lecturers = new ArrayList<>();

    /* images */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "logo_file_id",
            foreignKey = @ForeignKey(name = "fk_org_logo")
    )
    FileAsset logo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cover_file_id",
            foreignKey = @ForeignKey(name = "fk_org_cover")
    )
    FileAsset cover;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "banner_file_id",
            foreignKey = @ForeignKey(name = "fk_org_banner")
    )
    FileAsset banner;

    /* THESIS */

    // Years
    @OneToMany(
            mappedBy = "organization",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<AcademicYear> years = new ArrayList<>();

    @OneToMany(
            mappedBy = "organization",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<Semester> semesters = new ArrayList<>();

    @OneToMany(
            mappedBy = "organization",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<CouncilRole> role = new ArrayList<>();


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
