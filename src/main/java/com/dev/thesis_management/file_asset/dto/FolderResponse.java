package com.dev.thesis_management.file_asset.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FolderResponse {
    UUID id;
    String name;
    List<FileAssetResponse> files;
    List<FolderResponse> folders;
}
