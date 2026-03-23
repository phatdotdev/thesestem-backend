package com.dev.thesis_management.user.entity;


import com.dev.thesis_management.category.entity.AcademicYear;
import com.dev.thesis_management.category.entity.Course;
import com.dev.thesis_management.file_asset.entity.FileAsset;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.entity.Program;
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
        name = "students",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_org_code",
                        columnNames = {"organization_id", "student_code"}
                )
        }
)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Student {

    /* auth */
    @Id
    @GeneratedValue
    UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    Organization organization;

    /* student */
    @Column(name = "student_code", nullable = false)
    String studentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    Course course;

    /* contact */
    String fullName;
    LocalDate dob;
    String gender;
    String email;
    String phone;
    String address;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "avatar_file_id",
            foreignKey = @ForeignKey(name = "fk_std_avt")
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
