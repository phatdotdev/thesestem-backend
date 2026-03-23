package com.dev.thesis_management.thesis.mapper;

import com.dev.thesis_management.thesis.dto.group.MeetingResponse;
import com.dev.thesis_management.thesis.entity.Meeting;

public class MeetingMapper {
    public static MeetingResponse meetingToResponse(Meeting meeting){
        return MeetingResponse.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .startAt(meeting.getStartAt())
                .endAt(meeting.getEndAt())
                .url(meeting.getUrl())
                .build();
    }
}
