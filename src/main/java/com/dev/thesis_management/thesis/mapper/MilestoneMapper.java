package com.dev.thesis_management.thesis.mapper;

import com.dev.thesis_management.thesis.dto.milestone.MilestoneResponse;
import com.dev.thesis_management.thesis.entity.Milestone;

public class MilestoneMapper {
    public static MilestoneResponse toResponse(Milestone milestone){
        return new MilestoneResponse(
                milestone.getId(),
                milestone.getTitle(),
                milestone.getDescription(),
                milestone.getStartAt(),
                milestone.getEndAt()
        );
    }
}
