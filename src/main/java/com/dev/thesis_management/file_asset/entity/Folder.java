package com.dev.thesis_management.file_asset.entity;

import com.dev.thesis_management.thesis.entity.Group;
import com.dev.thesis_management.thesis.entity.Thesis;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "folders")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Folder {

    @Id
    @GeneratedValue
    UUID id;

    String name;

    // tree structure
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Folder parent;

    @OneToMany(
            mappedBy = "parent",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    Set<Folder> children = new HashSet<>();

    // files inside folder
    @OneToMany(
            mappedBy = "folder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    Set<FileAsset> files = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    Group group;

    @OneToOne(mappedBy = "folder")
    Thesis thesis;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // helper methods
    public void addChild(Folder folder){
        children.add(folder);
        folder.setParent(this);
    }

    public void removeChild(Folder folder){
        children.remove(folder);
        folder.setParent(null);
    }
}
