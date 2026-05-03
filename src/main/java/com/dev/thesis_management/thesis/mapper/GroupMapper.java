package com.dev.thesis_management.thesis.mapper;

import com.dev.thesis_management.thesis.dto.CreateGroupRequest;
import com.dev.thesis_management.thesis.dto.GroupResponse;
import com.dev.thesis_management.thesis.dto.UpdateGroupRequest;
import com.dev.thesis_management.thesis.entity.Group;
import com.dev.thesis_management.user.mapper.LecturerMapper;
import com.dev.thesis_management.user.mapper.StudentMapper;

import java.util.List;

public class GroupMapper {
    public static Group createGroupRequestToGroup(CreateGroupRequest request){
        return Group.builder()
                .name(request.name())
                .description(request.description())
                .build();
    }

    public static void updateGroupFromRequest(Group group, UpdateGroupRequest request){
        group.setName(request.name());
        group.setDescription(request.description());
    }

    public static GroupResponse toGroupResponse(Group group){
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .students(
                        group.getStudents() == null
                                ? List.of()
                                : group.getStudents().stream()
                                .map(StudentMapper::studentToResponse)
                                .toList()
                )
                .description(group.getDescription())
                .build();
    }

    public static GroupResponse toGroupResponseWithMentor(Group group){
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .students(
                        group.getStudents() == null
                                ? List.of()
                                : group.getStudents().stream()
                                .map(StudentMapper::studentToResponse)
                                .toList()
                )
                .description(group.getDescription())
                .mentor(
                        group.getMentor() == null
                                ? null
                                : LecturerMapper.lecturerToResponse(group.getMentor())
                )
                .build();
    }

}
