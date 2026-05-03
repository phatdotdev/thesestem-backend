package com.dev.thesis_management.thesis.dto.thesis;

import com.dev.thesis_management.thesis.dto.group.TopicResponse;
import com.dev.thesis_management.thesis.dto.organization.OrganizationResponse;
import com.dev.thesis_management.thesis.dto.submission.SubmissionResponse;
import com.dev.thesis_management.thesis.entity.Thesis;
import com.dev.thesis_management.user.dto.LecturerResponse;
import com.dev.thesis_management.user.dto.StudentResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThesisResponse {
    UUID id;
    String title;
    String titleEn;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(columnDefinition = "TEXT")
    String descriptionEn;

    Thesis.Status status;
    Thesis.AccessLevel accessLevel;
    Integer progressPercent;

    TopicResponse topic;
    StudentResponse student;
    LecturerResponse mentor;
    OrganizationResponse organization;

    List<SubmissionResponse> submissions;
}
