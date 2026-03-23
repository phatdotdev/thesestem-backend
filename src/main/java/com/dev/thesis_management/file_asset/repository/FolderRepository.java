package com.dev.thesis_management.file_asset.repository;

import com.dev.thesis_management.file_asset.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {
}
