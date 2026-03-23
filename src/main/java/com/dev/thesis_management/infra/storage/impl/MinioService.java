package com.dev.thesis_management.infra.storage.impl;

import com.dev.thesis_management.infra.storage.StorageService;
import com.dev.thesis_management.infra.storage.dto.StoredFile;
import com.dev.thesis_management.infra.storage.exception.StorageException;
import io.minio.*;
import io.minio.http.Method;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinioService implements StorageService {

    MinioClient minioClient;

    @NonFinal
    @Value("${minio.bucket}")
    String bucket;

    @NonFinal
    @Value("${minio.public-url}")
    String publicUrl;

    @Override
    public StoredFile upload(MultipartFile file, String folder) {
        try {
            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
            String path = folder + "/" + filename;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return StoredFile.builder()
                    .path(path)
                    .filename(filename)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .url(generatePublicUrl(path))
                    .build();

        } catch (Exception e) {
            throw new StorageException("Upload file failed", e);
        }
    }

    @Override
    public StoredFile upload(InputStream inputStream, String filename, String contentType, String folder) {
        try {
            String path = folder + "/" + filename;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .stream(inputStream, -1, 10 * 1024 * 1024)
                            .contentType(contentType)
                            .build()
            );

            return StoredFile.builder()
                    .path(path)
                    .filename(filename)
                    .contentType(contentType)
                    .url(generatePublicUrl(path))
                    .build();

        } catch (Exception e) {
            throw new StorageException("Upload stream failed", e);
        }
    }

    @Override
    public InputStream download(String path) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Download failed", e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Delete failed", e);
        }
    }

    @Override
    public String generatePublicUrl(String path) {
        return publicUrl + "/" + bucket + "/" + path;
    }

    @Override
    public String generatePresignedUrl(String path) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(path)
                            .expiry(60 * 10)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Generate presigned url failed", e);
        }
    }

}
