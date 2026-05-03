package com.dev.thesis_management.user.entity;

import com.dev.thesis_management.file_asset.entity.FileAsset;
import com.dev.thesis_management.organization.entity.College;
import com.dev.thesis_management.organization.entity.Department;
import com.dev.thesis_management.organization.entity.Faculty;
import com.dev.thesis_management.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "lecturers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_lecturer_org_code",
                        columnNames = {"organization_id", "lecturer_code"}
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Lecturer {

    /* auth */
    @Id
    @GeneratedValue
    UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    Organization organization;

    /* lecture */
    @Column(name = "lecturer_code", nullable = false)
    String lecturerCode;

    String fullName;
    LocalDate dob;
    String gender;
    String email;
    String phone;
    String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "college_id",
            foreignKey = @ForeignKey(
                    name = "fk_lecturer_college"
            ))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    College college;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "faculty_id",
            foreignKey = @ForeignKey(
            name = "fk_lecturer_faculty"
    ))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "department_id", foreignKey = @ForeignKey(
            name = "fk_lecturer_department"
    ))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    Department department;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "avatar_file_id",
            foreignKey = @ForeignKey(name = "fk_lec_avt")
    )
    FileAsset avatar;

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
