package com.dev.thesis_management.thesis.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "defense_scores",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"defense_id", "council_member_id"}
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DefenseScore {

    @Id
    @GeneratedValue
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defense_id", nullable = false)
    ThesisDefense defense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "council_member_id", nullable = false)
    CouncilMember councilMember;

    Double score;

    @Column(length = 1000)
    String comment;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @PrePersist
    public void prePersist(){
        createdAt = LocalDateTime.now();
    }
}
