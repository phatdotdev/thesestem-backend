package com.dev.thesis_management.file_asset.service;

import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.file_asset.dto.FileAssetResponse;
import com.dev.thesis_management.file_asset.entity.FileAsset;
import com.dev.thesis_management.file_asset.enums.FileType;
import com.dev.thesis_management.file_asset.repository.FileAssetRepository;
import com.dev.thesis_management.infra.storage.StorageService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file"
        );

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

    public FileAssetResponse download(UUID id){
        FileAsset file = fileAssetRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("File not found"));

        return FileAssetResponse.builder()
                .id(file.getId())
                .name(file.getOriginalName())
                .url(storageService.generatePresignedUrl(file.getPath()))
                .build();
    }

    public ResponseEntity<Resource> getFileBlob(UUID id) {
        FileAsset file = fileAssetRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("File not found"));

        InputStream inputStream = storageService.download(file.getPath());
        InputStreamResource resource = new InputStreamResource(inputStream);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (file.getContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(file.getContentType());
            } catch (Exception ignored) {
                // Fallback to octet-stream when stored content type is invalid.
            }
        }

        String filename = file.getOriginalName() != null
                ? file.getOriginalName()
                : "file-" + file.getId();

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(filename, StandardCharsets.UTF_8).build().toString()
                );

        responseBuilder.contentLength(file.getSize());

        return responseBuilder.body(resource);
    }

    public ResponseEntity<Resource> getFileForView(UUID id) {
        FileAsset file = fileAssetRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("File not found"));

        String ext = getFileExtension(file.getOriginalName()).toLowerCase();

        if (ext.equals("ppt") || ext.equals("pptx")) {
            return convertPptxToPdf(file);
        }

        return getFileBlob(id);
    }

    // Helper lấy extension
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? "" : filename.substring(lastDot + 1);
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

    public void deleteById(UUID assetId) {

        FileAsset asset = fileAssetRepository
                .findById(assetId)
                .orElseThrow(() -> new BadRequestException("File not found"));

        storageService.delete(asset.getPath());

        fileAssetRepository.delete(asset);
    }

    private ResponseEntity<Resource> convertPptxToPdf(FileAsset file) {
        File tempFile = null;

        try {
            // 1. Tạo file tạm
            String ext = getFileExtension(file.getOriginalName());
            tempFile = File.createTempFile("upload-", "." + ext);

            // 2. Ghi stream từ storage xuống file tạm
            try (InputStream in = storageService.download(file.getPath());
                 OutputStream out = new FileOutputStream(tempFile)) {
                in.transferTo(out);
            }

            // 3. Tạo resource từ file (QUAN TRỌNG)
            FileSystemResource resource = new FileSystemResource(tempFile);

            // 4. Build multipart request
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("files", resource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Accept", "application/pdf");

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // 5. Gọi Gotenberg
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    "http://localhost:4000/forms/libreoffice/convert",
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class
            );

            // 6. Validate response
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("Convert PPTX to PDF failed");
            }

            byte[] pdfBytes = response.getBody();

            String pdfFileName = file.getOriginalName()
                    .replaceAll("(?i)\\.pptx?$", ".pdf");

            // 7. Trả về PDF
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.inline()
                                    .filename(pdfFileName, StandardCharsets.UTF_8)
                                    .build()
                                    .toString())
                    .contentLength(pdfBytes.length)
                    .body(new ByteArrayResource(pdfBytes));

        } catch (Exception e) {
            System.out.println("Error converting PPTX to PDF: " + e);
            return getFileBlob(file.getId());
        } finally {
            // 8. Xóa file tạm (RẤT QUAN TRỌNG)
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}