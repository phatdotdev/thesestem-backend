package com.dev.thesis_management.thesis.entity;

import com.dev.thesis_management.organization.entity.College;
import com.dev.thesis_management.organization.entity.Department;
import com.dev.thesis_management.organization.entity.Faculty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "councils")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Council {
    @Id
    @GeneratedValue
    UUID id;

    String code;

    String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    Semester semester;

    @OneToMany(mappedBy = "council", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    Set<CouncilMember> members = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "college_id")
    College college;

    @ManyToOne
    @JoinColumn(name = "faculty_id")
    Faculty faculty;

    @ManyToOne
    @JoinColumn(name = "department_id")
    Department department;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    public void preUpdate() { updatedAt = LocalDateTime.now(); }
}
