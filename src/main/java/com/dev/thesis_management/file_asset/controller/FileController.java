package com.dev.thesis_management.file_asset.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.file_asset.dto.FileAssetResponse;
import com.dev.thesis_management.file_asset.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {

    FileService fileService;

    @GetMapping("/{id}/download")
    public ResponseEntity<ApiResponse<FileAssetResponse>> downloadFile(
            @PathVariable UUID id
            ){
        return ok(fileService.download(id));
    }

    @GetMapping("/{id}/blob")
    public ResponseEntity<Resource> getFileBlob(@PathVariable UUID id) {
        return fileService.getFileBlob(id);
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> getFileForView(@PathVariable UUID id) {
        return fileService.getFileForView(id);
    }
}
