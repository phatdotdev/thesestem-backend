package com.dev.thesis_management.organization.entity;

import com.dev.thesis_management.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "colleges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class College {
    @Id
    @GeneratedValue
    UUID id;

    @Column(nullable = false, unique = false)
    String code;

    @Column(nullable = false)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @OneToOne
    @JoinColumn(name = "manager_user_id", unique = true)
    User manager;

    /* relations */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "organization_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_college_org")
    )
    Organization organization;

    @OneToMany(
            mappedBy = "college",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<Faculty> faculties = new ArrayList<>();

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
