package com.dev.thesis_management.file_asset.dto;

import com.dev.thesis_management.file_asset.enums.FileType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileAssetResponse {
    UUID id;
    String name;
    String url;
    FileType type;

    Integer width;
    Integer height;
    Integer duration;

    boolean previewable;
}
