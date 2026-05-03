package com.dev.thesis_management.thesis.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.thesis.dto.milestone.MilestoneRequest;
import com.dev.thesis_management.thesis.dto.milestone.MilestoneResponse;
import com.dev.thesis_management.user.dto.MentorSearchForm;
import com.dev.thesis_management.thesis.dto.SemesterResponse;
import com.dev.thesis_management.user.dto.StudentSearchForm;
import com.dev.thesis_management.thesis.dto.UpdateSemesterRequest;
import com.dev.thesis_management.thesis.entity.Semester;
import com.dev.thesis_management.thesis.service.SemesterService;
import com.dev.thesis_management.user.dto.LecturerResponse;
import com.dev.thesis_management.user.dto.StudentResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;
import static com.dev.thesis_management.common.utils.UUIDUtils.*;

@RestController
@RequestMapping("/semesters")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SemesterController {
    SemesterService semesterService;

    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<ApiResponse<SemesterResponse>> updateSemesterStatus(
            Authentication authentication,
            @PathVariable UUID id,
            @PathVariable Semester.Status status
    ) {
        return ok(semesterService.updateSemesterStatus(id, status, parseUUID(authentication.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SemesterResponse>> updateSemester(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody UpdateSemesterRequest request
    ) {
        return ok(semesterService.updateSemester(id, request, parseUUID(authentication.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteSemester(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        semesterService.deleteSemester(id, parseUUID(authentication.getName()));
        return noContent();
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<SemesterResponse>> getCurrentSemester(
            Authentication authentication
    ) {
        return ok(semesterService.getCurrentSemester(parseUUID(authentication.getName())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SemesterResponse>>> getSemester(Authentication authentication){
        return ok(semesterService.getSemesters(parseUUID(authentication.getName())));
    }

    /* STUDENTS */

    @GetMapping("/current/students")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getCurrentSemesterStudents(
            StudentSearchForm form,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Authentication authentication
    ) {
        return ok(semesterService.listStudentsInSemester(form, parseUUID(authentication.getName())));
    }

    @GetMapping("/current/students/search")
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> searchCurrentSemesterStudents(
            StudentSearchForm form,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Authentication authentication
    ) {
        return ok(semesterService.searchStudentsInSemester(form, pageable, parseUUID(authentication.getName())));
    }

    @GetMapping("/{id}/students/search")
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> searchSemesterStudents(
            StudentSearchForm form,
            @PathVariable UUID id,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Authentication authentication
    ) {
        return ok(semesterService.searchStudentsBySemester(form, id, pageable, parseUUID(authentication.getName())));
    }

    @PutMapping("/current/students/{id}")
    public ResponseEntity<ApiResponse> addStudentToCurrentSemester(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        semesterService.addStudentToCurrentSemester(id, parseUUID(authentication.getName()));
        return noContent();
    }

    @DeleteMapping("/current/students/{id}")
    public ResponseEntity<ApiResponse> deleteStudentFromCurrentSemester(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        semesterService.removeStudentFromCurrentSemester(id, parseUUID(authentication.getName()));
        return noContent();
    }

    @GetMapping("/current/students/{id}")
    public ResponseEntity<ApiResponse<Boolean>> checkStudentInCurrentSemester(
            @PathVariable UUID id,
            Authentication authentication
    ){
        return ok(semesterService.checkStudentInSemester(id, parseUUID(authentication.getName())));
    }

    @GetMapping("/current/students/me")
    public ResponseEntity<ApiResponse<Boolean>> checkStudentInCurrentSemester(
            Authentication authentication
    ){
        return ok(semesterService.checkStudentInCurrentSemester(parseUUID(authentication.getName())));
    }

    /* LECTURERS */

    @GetMapping("/current/mentors")
    public ResponseEntity<ApiResponse<List<LecturerResponse>>> listCurrentSemesterMentors(
            MentorSearchForm form,
            Authentication authentication
    ) {
        return ok(semesterService.listMentorsInSemester(form, parseUUID(authentication.getName())));
    }

    @GetMapping("/current/mentors/search")
    public ResponseEntity<ApiResponse<Page<LecturerResponse>>> searchCurrentSemesterMentors(
            MentorSearchForm form,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Authentication authentication
    ) {
        return ok(semesterService.searchMentorsInSemester(form, pageable, parseUUID(authentication.getName())));
    }

    @GetMapping("/{id}/mentors/search")
    public ResponseEntity<ApiResponse<Page<LecturerResponse>>> searchSemesterMentors(
            MentorSearchForm form,
            @PathVariable UUID id,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Authentication authentication
    ) {
        return ok(semesterService.searchMentorsBySemester(form, id, pageable, parseUUID(authentication.getName())));
    }

    @PutMapping("/current/mentors")
    public ResponseEntity<ApiResponse> addMentorToCurrentSemester(
            Authentication authentication,
            @RequestBody UUID id
    ) {
        semesterService.addMentorToCurrentSemester(id, parseUUID(authentication.getName()));
        return noContent();
    }

    @DeleteMapping("/current/mentors")
    public ResponseEntity<ApiResponse> deleteMentorFromCurrentSemester(
            Authentication authentication,
            @RequestBody UUID id
    ) {
        semesterService.removeMentorFromCurrentSemester(id, parseUUID(authentication.getName()));
        return noContent();
    }

    @GetMapping("/current/mentors/{id}")
    public ResponseEntity<ApiResponse<Boolean>> checkMentorInCurrentSemester(
            @PathVariable UUID id,
            Authentication authentication
    ){
        return ok(semesterService.checkMentorInCurrentSemester(id, parseUUID(authentication.getName())));
    }

    @GetMapping("/current/mentors/me")
    public ResponseEntity<ApiResponse<Boolean>> checkMentorInCurrentSemester(
            Authentication authentication
    ){
        return ok(semesterService.checkMentorInCurrentSemester(parseUUID(authentication.getName())));
    }

    @PostMapping("/current/milestones")
    public ResponseEntity<ApiResponse> createMilestone(
            @RequestBody MilestoneRequest request,
            Authentication auth
    ){
        semesterService.createMilestone(
                request,
                parseUUID(auth.getName())
        );

        return noContent();
    }

    @PutMapping("/current/milestones/{milestoneId}")
    public ResponseEntity<ApiResponse> updateMilestone(
            @PathVariable UUID milestoneId,
            @RequestBody MilestoneRequest request,
            Authentication auth
    ){
        semesterService.updateMilestone(
                milestoneId,
                request,
                parseUUID(auth.getName())
        );

        return noContent();
    }

    @DeleteMapping("/{id}/milestones/{milestoneId}")
    public ResponseEntity<ApiResponse> deleteMilestone(
            @PathVariable UUID id,
            @PathVariable UUID milestoneId,
            Authentication auth
    ){
        semesterService.deleteMilestone(
                id,
                milestoneId,
                parseUUID(auth.getName())
        );

        return noContent();
    }

    @GetMapping("/{id}/milestones")
    public ResponseEntity<ApiResponse<List<MilestoneResponse>>> getMilestones(
            @PathVariable UUID id,
            Authentication auth
    ){
        return ok(
                semesterService.getMilestones(
                        id,
                        parseUUID(auth.getName())
                )
        );
    }

    @GetMapping("/current/milestones")
    public ResponseEntity<ApiResponse<List<MilestoneResponse>>> getCurrentMilestones(
            Authentication auth
    ){
        return ok(
                semesterService.getCurrentMilestones(
                        parseUUID(auth.getName())
                )
        );
    }
}

