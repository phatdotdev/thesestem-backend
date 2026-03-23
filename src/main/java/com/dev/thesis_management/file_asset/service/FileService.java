package com.dev.thesis_management.file_asset.service;

import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.file_asset.entity.FileAsset;
import com.dev.thesis_management.file_asset.enums.FileType;
import com.dev.thesis_management.file_asset.repository.FileAssetRepository;
import com.dev.thesis_management.infra.storage.StorageService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {

    StorageService storageService;
    FileAssetRepository fileAssetRepository;

    public FileAsset upload(MultipartFile file, UUID ownerId) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        // limit size 20MB
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new BadRequestException("File size must be <= 20MB");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename());

        FileType type = detectType(file);

        var stored = storageService.upload(file, resolveFolder(type));

        FileAsset asset = FileAsset.builder()
                .originalName(originalName)
                .path(stored.getPath())
                .contentType(file.getContentType())
                .size(file.getSize())
                .url(stored.getUrl())
                .type(type)
                .ownerId(ownerId)
                .build();

        return fileAssetRepository.save(asset);
    }

    private FileType detectType(MultipartFile file) {

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.contains(".")) {
            return FileType.OTHER;
        }

        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        return switch (ext) {

            case "png", "jpg", "jpeg", "gif", "webp" -> FileType.IMAGE;

            case "mp4", "mov", "avi", "mkv" -> FileType.VIDEO;

            case "pdf" -> FileType.PDF;

            case "doc", "docx" -> FileType.WORD;

            case "xls", "xlsx", "csv" -> FileType.EXCEL;

            case "ppt", "pptx" -> FileType.POWERPOINT;

            case "java", "js", "ts", "py", "cpp", "c", "cs", "go", "rs", "php" -> FileType.CODE;

            case "zip", "rar", "7z", "tar", "gz" -> FileType.ARCHIVE;

            default -> FileType.OTHER;
        };
    }

    private String resolveFolder(FileType type) {

        return switch (type) {

            case IMAGE -> "images";

            case VIDEO -> "videos";

            case PDF, WORD, EXCEL, POWERPOINT -> "documents";

            case CODE -> "codes";

            case ARCHIVE -> "archives";

            default -> "others";
        };
    }

    public void delete(UUID assetId, UUID ownerId) {

        FileAsset asset = fileAssetRepository
                .findById(assetId)
                .orElseThrow(() -> new BadRequestException("File not found"));

        if (!asset.getOwnerId().equals(ownerId)) {
            throw new BadRequestException("You are not allowed to delete this file");
        }

        storageService.delete(asset.getPath());

        fileAssetRepository.delete(asset);
    }
}