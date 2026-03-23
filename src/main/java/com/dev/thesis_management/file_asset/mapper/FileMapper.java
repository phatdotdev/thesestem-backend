package com.dev.thesis_management.file_asset.mapper;

import com.dev.thesis_management.file_asset.dto.FileAssetResponse;
import com.dev.thesis_management.file_asset.dto.FolderResponse;
import com.dev.thesis_management.file_asset.entity.FileAsset;
import com.dev.thesis_management.file_asset.entity.Folder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FileMapper {

    public static FileAssetResponse toFileResponse(FileAsset file) {
        if (file == null) return null;

        return FileAssetResponse.builder()
                .id(file.getId())
                .name(file.getOriginalName())
                .url(file.getUrl())
                .type(file.getType())
                .width(file.getWidth())
                .height(file.getHeight())
                .duration(file.getDuration())
                .build();
    }

    public static FolderResponse toFolderResponse(Folder folder) {
        if (folder == null) return null;

        List<FileAssetResponse> files = folder.getFiles() == null
                ? Collections.emptyList()
                : folder.getFiles()
                .stream()
                .map(FileMapper::toFileResponse)
                .collect(Collectors.toList());

        List<FolderResponse> folders = folder.getChildren() == null
                ? Collections.emptyList()
                : folder.getChildren()
                .stream()
                .map(FileMapper::toFolderResponse)
                .collect(Collectors.toList());

        return new FolderResponse(
                folder.getId(),
                folder.getName(),
                files,
                folders
        );
    }
}