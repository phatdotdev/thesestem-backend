package com.dev.thesis_management.thesis.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.thesis.dto.CreateGroupRequest;
import com.dev.thesis_management.thesis.dto.GroupResponse;
import com.dev.thesis_management.thesis.dto.UpdateGroupRequest;
import com.dev.thesis_management.thesis.dto.group.*;
import com.dev.thesis_management.thesis.dto.thesis.ThesisResponse;
import com.dev.thesis_management.thesis.service.GroupService;
import com.dev.thesis_management.user.dto.StudentResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;
import static com.dev.thesis_management.common.utils.UUIDUtils.*;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupController {

    GroupService groupService;

    /* =========================================================
                            GROUP
    ========================================================= */

    @GetMapping("/mentor/current")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getCurrentMentorGroups(
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        return ok(groupService.getCurrentMentorGroups(userId));
    }

    @GetMapping("/student/current")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getCurrentStudentGroups(
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        return ok(groupService.getCurrentStudentGroups(userId));
    }

    @GetMapping("/mentor/semester/{id}")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getMentorGroupsBySemester(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        return ok(groupService.getMentorGroupsBySemester(id, userId));
    }

    @GetMapping("/student/semester/{id}")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getStudentGroupsBySemester(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        return ok(groupService.getStudentGroupsBySemester(id, userId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(
            @RequestBody CreateGroupRequest request,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        return created(groupService.createGroup(request, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroupById(
            @PathVariable UUID id
    ) {
        return ok(groupService.getGroupById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(
            @PathVariable UUID id,
            @RequestBody UpdateGroupRequest request,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        return ok(groupService.updateGroup(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteGroup(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        groupService.deleteGroup(id, userId);
        return noContent();
    }

    /* =========================================================
                        ASSIGNMENTS
    ========================================================= */

    @GetMapping("/{id}/assignments")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getGroupAssignments(
            @PathVariable UUID id
    ) {
        return ok(groupService.getAssignmentGroup(id));
    }

    @PostMapping("/{id}/assignments")
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @PathVariable UUID id,
            @RequestBody CreateAssignmentRequest request,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        return created(groupService.createAssignment(id, request, userId));
    }

    @PutMapping("/{id}/assignments/{assignmentId}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateAssignment(
            @PathVariable UUID id,
            @PathVariable UUID assignmentId,
            @RequestBody UpdateAssignmentRequest request,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        return ok(groupService.updateAssignment(id, assignmentId, request, userId));
    }

    @DeleteMapping("/{id}/assignments/{assignmentId}")
    public ResponseEntity<ApiResponse> deleteAssignment(
            @PathVariable UUID id,
            @PathVariable UUID assignmentId,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        groupService.deleteAssignment(id, assignmentId, userId);
        return noContent();
    }

    /* =========================================================
                        MEETINGS
    ========================================================= */

    @GetMapping("/{id}/meetings")
    public ResponseEntity<ApiResponse<List<MeetingResponse>>> getGroupMeetings(
            @PathVariable UUID id
    ) {
        return ok(groupService.getMeetingGroup(id));
    }

    @PostMapping("/{id}/meetings")
    public ResponseEntity<ApiResponse<MeetingResponse>> createMeeting(
            @PathVariable UUID id,
            @RequestBody MeetingRequest request,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        return created(groupService.createMeeting(id, request, userId));
    }

    @PutMapping("/{id}/meetings/{meetingId}")
    public ResponseEntity<ApiResponse<MeetingResponse>> updateMeeting(
            @PathVariable UUID id,
            @PathVariable UUID meetingId,
            @RequestBody MeetingRequest request,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        return ok(groupService.updateMeeting(meetingId, request, userId));
    }

    @DeleteMapping("/{id}/meetings/{meetingId}")
    public ResponseEntity<ApiResponse> deleteMeeting(
            @PathVariable UUID id,
            @PathVariable UUID meetingId,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        groupService.deleteMeeting(meetingId, userId);
        return noContent();
    }

    /* =========================================================
                        DOCUMENTS
    ========================================================= */

    /* =========================================================
                            STUDENTS
    ========================================================= */

    @GetMapping("/{groupId}/students")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getGroupStudents(
            @PathVariable UUID groupId,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        return ok(groupService.getStudents(groupId, userId));
    }

    @PutMapping("/{groupId}/students/{studentId}")
    public ResponseEntity<ApiResponse> addStudentToGroup(
            @PathVariable UUID groupId,
            @PathVariable UUID studentId,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        groupService.addStudentToGroup(studentId, groupId, userId);
        return noContent();
    }

    @DeleteMapping("/{groupId}/students/{studentId}")
    public ResponseEntity<ApiResponse> removeStudentFromGroup(
            @PathVariable UUID groupId,
            @PathVariable UUID studentId,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        groupService.removeStudentFromGroup(studentId, groupId, userId);
        return noContent();
    }

    /* =========================================================
                            TOPICS
    ========================================================= */

    @GetMapping("/{id}/topics")
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getGroupTopics(
            @PathVariable UUID id
    ) {
        return ok(groupService.getGroupTopics(id));
    }

    @PostMapping("/{id}/topics")
    public ResponseEntity<ApiResponse<TopicResponse>> createTopic(
            @PathVariable UUID id,
            @RequestBody CreateTopicRequest request,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        return created(groupService.createTopic(id, request, userId));
    }

    @PutMapping("/{id}/topics/{topicId}")
    public ResponseEntity<ApiResponse<TopicResponse>> updateTopic(
            @PathVariable UUID id,
            @PathVariable UUID topicId,
            @RequestBody UpdateTopicRequest request,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        return ok(groupService.updateTopic(id, topicId, request, userId));
    }

    @PostMapping("/{id}/topics/{topicId}/{studentId}")
    public ResponseEntity<ApiResponse> assignStudentToTopic(
            @PathVariable UUID id,
            @PathVariable UUID topicId,
            @PathVariable UUID studentId,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        groupService.assignStudentToTopic(id, topicId, studentId, userId);
        return noContent();
    }

    @DeleteMapping("/{id}/topics/{topicId}")
    public ResponseEntity<ApiResponse> deleteTopic(
            @PathVariable UUID id,
            @PathVariable UUID topicId,
            Authentication authentication
    ) {
        UUID userId = parseUUID(authentication.getName());
        groupService.removeTopic(id, topicId, userId);
        return noContent();
    }

    /* =========================================================
                            THESES
    ========================================================= */

    @GetMapping("/{id}/theses")
    public ResponseEntity<ApiResponse<List<ThesisResponse>>> getGroupTheses(
            @PathVariable UUID id
    ) {
        return ok(groupService.getGroupTheses(id));
    }
}