package com.dev.thesis_management.file_asset.service;

import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.file_asset.entity.FileAsset;
import com.dev.thesis_management.file_asset.entity.Folder;
import com.dev.thesis_management.file_asset.repository.FileAssetRepository;
import com.dev.thesis_management.file_asset.repository.FolderRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FolderService {

    FolderRepository folderRepository;
    FileAssetRepository fileAssetRepository;

    @Transactional
    public Folder createFolder(String name, UUID parentId) {

        Folder parent = null;

        if (parentId != null) {
            parent = folderRepository.findById(parentId)
                    .orElseThrow(() -> new BadRequestException("Parent folder not found"));
        }

        Folder folder = Folder.builder()
                .name(name)
                .parent(parent)
                .build();

        return folderRepository.save(folder);
    }

    @Transactional
    public Folder createSubFolder(UUID parentId, String name) {

        Folder parent = folderRepository.findById(parentId)
                .orElseThrow(() -> new BadRequestException("Parent folder not found"));

        Folder folder = Folder.builder()
                .name(name)
                .parent(parent)
                .build();

        return folderRepository.save(folder);
    }

    @Transactional
    public Folder renameFolder(UUID folderId, String newName) {

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new BadRequestException("Folder not found"));

        folder.setName(newName);

        return folderRepository.save(folder);
    }

    @Transactional
    public void deleteFolder(UUID folderId) {

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new BadRequestException("Folder not found"));

        if (folder.getParent() == null) {
            throw new BadRequestException("Cannot delete root folder");
        }

        folderRepository.delete(folder);
    }

    @Transactional
    public FileAsset addFile(UUID folderId, FileAsset file) {

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new BadRequestException("Folder not found"));

        file.setFolder(folder);

        return fileAssetRepository.save(file);
    }

    @Transactional
    public void deleteFile(UUID fileId) {

        FileAsset file = fileAssetRepository.findById(fileId)
                .orElseThrow(() -> new BadRequestException("File not found"));

        fileAssetRepository.delete(file);
    }
}