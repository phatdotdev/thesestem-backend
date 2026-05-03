package com.dev.thesis_management.thesis.mapper;

import com.dev.thesis_management.file_asset.mapper.FileMapper;
import com.dev.thesis_management.thesis.dto.group.AssignmentResponse;
import com.dev.thesis_management.thesis.dto.group.AssignmentSubmissionResponse;
import com.dev.thesis_management.thesis.entity.Assignment;
import com.dev.thesis_management.thesis.entity.AssignmentSubmission;
import com.dev.thesis_management.user.mapper.StudentMapper;

public class AssignmentMapper {
    public static AssignmentResponse assignmentToResponse(Assignment assignment){
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .name(assignment.getName())
                .description(assignment.getDescription())
                .deadline(assignment.getDeadline())
                .status(assignment.getStatus().name())
                .build();
    }

    public static AssignmentSubmissionResponse assignmentSubmissionResponse(AssignmentSubmission submission){
        return AssignmentSubmissionResponse.builder()
                .id(submission.getId())
                .student(StudentMapper.studentToResponse(submission.getStudent()))
                .files(submission.getFiles() != null ? submission.getFiles().stream().map(FileMapper::toFileResponse).toList() : null)
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
}
