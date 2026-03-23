package com.dev.thesis_management.thesis.mapper;

import com.dev.thesis_management.thesis.dto.group.AssignmentResponse;
import com.dev.thesis_management.thesis.entity.Assignment;

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
}
