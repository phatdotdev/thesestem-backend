package com.dev.thesis_management.thesis.dto.submission;

import com.dev.thesis_management.file_asset.dto.FileAssetResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    UUID id;
    UUID thesisId;

    Integer version;

    String note;

    LocalDateTime submittedAt;

    List<FileAssetResponse> files;
}
