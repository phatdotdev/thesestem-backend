package com.dev.thesis_management.thesis.dto.group;

import com.dev.thesis_management.file_asset.dto.FileAssetResponse;
import com.dev.thesis_management.user.dto.StudentResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentSubmissionResponse {
    UUID id;
    StudentResponse student;
    List<FileAssetResponse> files;
    LocalDateTime submittedAt;
}
