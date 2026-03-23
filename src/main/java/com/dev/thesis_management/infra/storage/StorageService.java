package com.dev.thesis_management.infra.storage;

import com.dev.thesis_management.infra.storage.dto.StoredFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {

    StoredFile upload(MultipartFile file, String folder);

    StoredFile upload(InputStream inputStream, String fileName, String contentType, String folder);

    InputStream download(String path);

    void delete(String path);

    String generatePublicUrl(String path);

    String generatePresignedUrl(String path);
}
