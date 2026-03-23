package com.dev.thesis_management.user.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.organization.dto.ManagerSearchForm;
import com.dev.thesis_management.user.dto.MentorSearchForm;
import com.dev.thesis_management.user.dto.StudentSearchForm;
import com.dev.thesis_management.user.dto.*;
import com.dev.thesis_management.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;
import static com.dev.thesis_management.common.utils.UUIDUtils.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    /* STUDENT */

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudents(Authentication authentication){
        return ok(userService.getStudents(parseUUID(authentication.getName())));
    }

    @PostMapping("/students")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            Authentication authentication,
            @RequestBody CreateStudentRequest request) {
        return created(userService.createStudent(request, parseUUID(authentication.getName())));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody UpdateStudentRequest request
    ) {
        return ok(userService.updateStudent(id, request, parseUUID(authentication.getName())));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<ApiResponse> deleteStudent(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        userService.deleteStudent(id, parseUUID(authentication.getName()));
        return noContent();
    }

    @GetMapping("/students/profile")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentProfile(
            Authentication authentication
    ){
        return ok(userService.getStudentProfile(parseUUID(authentication.getName())));
    }

    @GetMapping("/students/search")
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> searchStudents(
            StudentSearchForm form,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Authentication authentication
    ){
        return ok(userService.searchStudents(form, pageable, parseUUID(authentication.getName())));
    }

    @PutMapping("/students/profile")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudentProfile(
        Authentication authentication,
        @RequestBody UpdateStudentRequest request
    ){
        return ok(userService.updateStudentProfile(request, parseUUID(authentication.getName())));
    }

    @PutMapping("/students/profile/avatar")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudentAvatar(
            Authentication authentication,
            @RequestParam("file")MultipartFile file
            ){
        return ok(userService.updateStudentAvatar(file, parseUUID(authentication.getName())));
    }

    /* LECTURER */
    @GetMapping("/lecturers")
    public ResponseEntity<ApiResponse<List<LecturerResponse>>> getLecturers(
            MentorSearchForm form,
            Authentication authentication){
        return ok(userService.getLecturers(form, parseUUID(authentication.getName())));
    }

    @GetMapping("/lecturers/search")
    public ResponseEntity<ApiResponse<Page<LecturerResponse>>> searchLecturers(
            MentorSearchForm form,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Authentication authentication
    ){
        return ok(userService.searchLecturers(form, pageable, parseUUID(authentication.getName())));
    }

    @PostMapping("/lecturers")
    public ResponseEntity<ApiResponse<LecturerResponse>> createLecturer(
            Authentication authentication,
            @RequestBody CreateLecturerRequest request
    ){
        return created(userService.createLecturer(request, parseUUID(authentication.getName())));
    }

    @PutMapping("/lecturers/{id}")
    public ResponseEntity<ApiResponse<LecturerResponse>> updateLecturer(
        Authentication authentication,
        @PathVariable UUID id,
        @RequestBody UpdateLecturerRequest request
    ){
        return ok(userService.updateLecturer(id, request, parseUUID(authentication.getName())));
    }

    @DeleteMapping("/lecturers/{id}")
    public ResponseEntity<ApiResponse> deleteLecturer(
            Authentication authentication,
            @PathVariable UUID id
    ){
        userService.deleteLecturer(id, parseUUID(authentication.getName()));
        return noContent();
    }

    @GetMapping("/lecturers/profile")
    public ResponseEntity<ApiResponse<LecturerResponse>> getLecturerProfile(
            Authentication authentication
    ){
        return ok(userService.getLecturerProfile(parseUUID(authentication.getName())));
    }

    @PutMapping("/lecturers/profile")
    public ResponseEntity<ApiResponse<LecturerResponse>> updateLecturerProfile(
            Authentication authentication,
            UpdateLecturerRequest request
    ){
        return ok(userService.updateLecturerProfile(request, parseUUID(authentication.getName())));
    }

    @PutMapping("/lecturers/profile/avatar")
    public ResponseEntity<ApiResponse<LecturerResponse>> updateLecturerAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ){
        return ok(userService.updateLecturerAvatar(file, parseUUID(authentication.getName())));
    }

    /* ADMINS */

    @GetMapping("/managers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ManagerResponse>>> getManagers(
            ManagerSearchForm form,
            Pageable pageable,
            Authentication authentication
    ){
        return ok(userService.searchManagers(form, pageable, parseUUID(authentication.getName())));
    }

    @PostMapping("/managers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ManagerResponse>> createManager(
            @RequestBody ManagerRequest request,
            Authentication authentication
    ){
        return created(userService.createManager(request, parseUUID(authentication.getName())));
    }

    @PutMapping("/managers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ManagerResponse>> updateManager(
            @PathVariable UUID id,
            @RequestBody ManagerRequest request,
            Authentication authentication
    ){
        return ok(userService.updateManager(id, request, parseUUID(authentication.getName())));
    }

    @DeleteMapping("/managers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteManager(
            @PathVariable UUID id,
            Authentication authentication
    ){
        userService.deleteManager(id, parseUUID(authentication.getName()));
        return noContent();
    }
}
