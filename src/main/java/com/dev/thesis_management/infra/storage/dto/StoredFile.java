package com.dev.thesis_management.infra.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredFile {
    String path;
    String filename;
    String contentType;
    long size;
    String url;
}
