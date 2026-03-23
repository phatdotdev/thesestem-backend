package com.dev.thesis_management.file_asset.entity;

import com.dev.thesis_management.file_asset.enums.FileType;
import com.dev.thesis_management.thesis.entity.ThesisSubmission;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_assets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileAsset {
    @Id
    @GeneratedValue
    UUID id;

    @Column(nullable = false)
    String originalName;

    @Column(nullable = false)
    String path;

    String contentType;

    long size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    FileType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    ThesisSubmission submission;

    /* media metadata */
    Integer width;
    Integer height;
    Integer duration;

    /* access */
    String url;
    boolean publicAccess = true;

    /* owner */
    UUID ownerId;

    /* audit */
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
