package com.dev.thesis_management.thesis.entity;

import com.dev.thesis_management.file_asset.entity.Folder;
import com.dev.thesis_management.user.entity.Student;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "theses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Thesis {

    @Id
    @GeneratedValue
    UUID id;

    String title;

    @Column(name = "title_en")
    String titleEn;

    String description;

    String descriptionEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    Student student;

    // STATUS
    @Enumerated(EnumType.STRING)
    Status status;
    @Column(name = "progress_percent")
    Integer progressPercent = 0;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "folder_id")
    Folder folder;

    @OneToMany(mappedBy = "thesis", cascade = CascadeType.ALL)
    List<ThesisSubmission> submissions;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public enum Status {
        PROPOSAL,
        IN_PROGRESS,
        SUBMITTED,
        GRADED
    }
}