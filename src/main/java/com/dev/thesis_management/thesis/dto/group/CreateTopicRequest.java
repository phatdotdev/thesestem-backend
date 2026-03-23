package com.dev.thesis_management.thesis.dto.group;

public record CreateTopicRequest(
        String title,
        Integer maxStudents,
        String description
) {
}
