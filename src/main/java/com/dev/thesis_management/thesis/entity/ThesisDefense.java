package com.dev.thesis_management.thesis.entity;

import com.dev.thesis_management.user.entity.Student;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "thesis_defenses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ThesisDefense {

    @Id
    @GeneratedValue
    UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thesis_id", nullable = false)
    Thesis thesis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "council_id", nullable = false)
    Council council;

    @Column(name = "defense_time")
    LocalDateTime defenseTime;

    String location;

    @OneToMany(
            mappedBy = "defense",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    Set<DefenseScore> scores = new HashSet<>();

    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @PrePersist
    public void prePersist(){
        createdAt = LocalDateTime.now();
    }

    public enum Status{
        SCHEDULED,
        COMPLETED,
        CANCELLED
    }
}