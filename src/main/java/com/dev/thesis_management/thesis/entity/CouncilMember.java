package com.dev.thesis_management.thesis.entity;

import com.dev.thesis_management.user.entity.Lecturer;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "council_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"council_id", "lecturer_id"}),
                @UniqueConstraint(columnNames = {"council_id", "role_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouncilMember {

    @Id
    @GeneratedValue
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "council_id", nullable = false)
    Council council;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecturer_id", nullable = false)
    Lecturer lecturer;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    CouncilRole role;
}

