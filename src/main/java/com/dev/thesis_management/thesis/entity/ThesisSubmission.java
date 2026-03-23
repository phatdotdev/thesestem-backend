package com.dev.thesis_management.thesis.entity;

import com.dev.thesis_management.file_asset.entity.FileAsset;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "thesis_submissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThesisSubmission {
    @Id
    @GeneratedValue
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thesis_id", nullable = false)
    Thesis thesis;

    @Column(name = "version")
    Integer version;

    @Column(name = "submitted_at")
    LocalDateTime submittedAt;

    String note;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    List<FileAsset> files;

    @PrePersist
    void prePersist() {
        submittedAt = LocalDateTime.now();
    }
}
