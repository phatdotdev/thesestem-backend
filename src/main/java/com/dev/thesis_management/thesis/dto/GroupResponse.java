package com.dev.thesis_management.thesis.dto;

import com.dev.thesis_management.user.dto.LecturerResponse;
import com.dev.thesis_management.user.dto.StudentResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupResponse {
    UUID id;
    String name;
    String description;
    LecturerResponse mentor;
    List<StudentResponse> students;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
