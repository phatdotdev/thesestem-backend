package com.dev.thesis_management.ai_integration.service;

import com.dev.thesis_management.common.utils.MimeTypeUtils;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.file_asset.entity.FileAsset;
import com.dev.thesis_management.file_asset.repository.FileAssetRepository;
import com.dev.thesis_management.infra.storage.StorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnalyzeFileService {

    StorageService storageService;
    FileAssetRepository fileAssetRepository;
    GeminiService geminiService;

    public String analyzeFile(UUID fileId, String userPrompt) {
        FileAsset file = fileAssetRepository.findById(fileId)
                .orElseThrow(() -> new BadRequestException("File not found"));

        byte[] fileBytes;
        try (InputStream stream = storageService.download(file.getPath())) {
            fileBytes = stream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Không đọc được file", e);
        }
        String contentType = Optional.ofNullable(file.getContentType())
                .orElse(MimeTypeUtils.fromExtension(file.getOriginalName()));

        System.out.printf("ContentType: %s%n", contentType);
        System.out.printf("Prompt: %s%n", userPrompt);
        System.out.printf("File size: %d%n", fileBytes.length);

        return geminiService.analyzeFile(fileBytes, contentType, userPrompt);
    }

}
