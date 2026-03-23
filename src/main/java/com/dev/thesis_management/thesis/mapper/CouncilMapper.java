package com.dev.thesis_management.thesis.mapper;

import com.dev.thesis_management.thesis.dto.council.CouncilMemberResponse;
import com.dev.thesis_management.thesis.dto.council.CouncilResponse;
import com.dev.thesis_management.thesis.entity.Council;
import com.dev.thesis_management.thesis.entity.CouncilMember;
import com.dev.thesis_management.user.mapper.LecturerMapper;

import java.util.UUID;

public class CouncilMapper {
    public static CouncilResponse toCouncilResponse(Council council){
        return CouncilResponse.builder()
                .id(council.getId())
                .name(council.getName())
                .code(council.getCode())
                .members(council.getMembers()
                        .stream().map(CouncilMapper::toMemberResponse)
                        .toList())
                .build();
    }

    public static CouncilResponse toCouncilResponse(Council council, UUID lecturerId) {
        return CouncilResponse.builder()
                .id(council.getId())
                .name(council.getName())
                .code(council.getCode())
                .members(council.getMembers()
                        .stream().map(member -> toMemberResponse(member, lecturerId))
                        .toList())
                .build();
    }
    public static CouncilMemberResponse toMemberResponse(CouncilMember member){
        return CouncilMemberResponse.builder()
                .id(member.getId())
                .lecturer(LecturerMapper.lecturerToResponse(member.getLecturer()))
                .role(member.getRole())
                .build();
    }

    public static CouncilMemberResponse toMemberResponse(CouncilMember cm, UUID currentLecturerId) {
        return CouncilMemberResponse.builder()
                .id(cm.getId())
                .lecturer(LecturerMapper.lecturerToResponse(cm.getLecturer()))
                .role(cm.getRole())
                .isCurrentUser(cm.getLecturer().getId().equals(currentLecturerId))
                .build();
    }
}
