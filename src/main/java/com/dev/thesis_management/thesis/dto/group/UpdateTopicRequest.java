package com.dev.thesis_management.thesis.dto.group;

public record UpdateTopicRequest(
        String title,
        Integer maxStudents,
        String description
) {
}
