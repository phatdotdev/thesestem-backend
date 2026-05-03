package com.dev.thesis_management.thesis.mapper;

import com.dev.thesis_management.thesis.dto.group.TopicResponse;
import com.dev.thesis_management.thesis.entity.Topic;
import com.dev.thesis_management.user.mapper.StudentMapper;

import java.util.List;

public class TopicMapper {
    public static TopicResponse topicToResponse(Topic topic){
        return TopicResponse.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .description(topic.getDescription())
                .maxStudents(topic.getMaxStudents())
                .currentStudents(topic.getTheses() != null ? topic.getTheses().size() : 0)
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .build();
    }
}
