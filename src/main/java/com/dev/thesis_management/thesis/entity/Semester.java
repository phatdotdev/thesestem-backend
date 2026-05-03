package com.dev.thesis_management.thesis.entity;

import com.dev.thesis_management.category.entity.AcademicYear;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.user.entity.Lecturer;
import com.dev.thesis_management.user.entity.Student;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "semesters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Semester {
    @Id
    @GeneratedValue
    UUID id;

    String name;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    @Enumerated(EnumType.STRING)
    Status status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    AcademicYear academicYear;

    // Student List
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "semester_student",
            joinColumns = @JoinColumn(name = "semester_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    List<Student> students;

    // Lecturer List
    @ManyToMany
    @JoinTable(
            name = "semester_lecturer",
            joinColumns = @JoinColumn(name = "semester_id"),
            inverseJoinColumns = @JoinColumn(name = "lecturer_id")
    )
    List<Lecturer> mentors;

    // Group List
    @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Group> groups = new ArrayList<>();

    // Mile List
    @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Milestone> milestones = new ArrayList<>();

    // Council List => Result

    // Topics

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    public void preUpdate() { updatedAt = LocalDateTime.now(); }

    public enum Status { UPCOMING, ACTIVE, FINISHED }
}
