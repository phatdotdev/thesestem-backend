package com.dev.thesis_management.thesis.mapper;

import com.dev.thesis_management.file_asset.mapper.FileMapper;
import com.dev.thesis_management.thesis.dto.submission.SubmissionResponse;
import com.dev.thesis_management.thesis.entity.ThesisSubmission;

import java.util.Collections;
import java.util.stream.Collectors;

public class SubmissionMapper {

    public static SubmissionResponse toResponse(ThesisSubmission submission){

        return SubmissionResponse.builder()
                .id(submission.getId())
                .thesisId(submission.getThesis().getId())
                .version(submission.getVersion())
                .note(submission.getNote())
                .submittedAt(submission.getSubmittedAt())
                .files(
                        submission.getFiles() == null
                                ? Collections.emptyList()
                                : submission.getFiles()
                                .stream()
                                .map(FileMapper::toFileResponse)
                                .collect(Collectors.toList())
                )
                .build();
    }
}