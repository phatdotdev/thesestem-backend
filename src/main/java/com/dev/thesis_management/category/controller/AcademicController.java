package com.dev.thesis_management.category.controller;

import com.dev.thesis_management.category.dto.AddSemesterRequest;
import com.dev.thesis_management.category.entity.AcademicYear;
import com.dev.thesis_management.category.service.AcademicService;
import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.thesis.dto.SemesterResponse;
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
@RequestMapping("/years")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AcademicController {

    AcademicService academicService;

    @GetMapping
    ResponseEntity<ApiResponse<List<AcademicYear>>> getYears(
            Authentication authentication
    ){
        return ok(academicService.getAllAcademicYears(parseUUID(authentication.getName())));
    }

    @PostMapping
    ResponseEntity<ApiResponse<AcademicYear>> addYear(
            Authentication authentication,
           @RequestBody AcademicYear request
    ){
        return created(academicService.addAcademicYear(request, parseUUID(authentication.getName())));
    }

    @PutMapping("/{id}")
    ResponseEntity<ApiResponse<AcademicYear>> updateYear(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody AcademicYear request
            ){
        return ok(academicService.updateAcademicYear(id, request, parseUUID(authentication.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteYear(
            Authentication authentication,
            @PathVariable UUID id
    ){
        academicService.deleteAcademicYear(id, parseUUID(authentication.getName()));
        return noContent();
    }

    @GetMapping("/{id}/semesters")
    public ResponseEntity<ApiResponse<List<SemesterResponse>>> getSemesters(
            Authentication authentication,
            @PathVariable UUID id
    ){
        return ok(academicService.getSemesters(id, parseUUID(authentication.getName())));
    }

    @PostMapping("/{id}/semesters")
    public ResponseEntity<ApiResponse<SemesterResponse>> addSemester(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody AddSemesterRequest request
            ){
        return ok(academicService.addSemester(id, request, parseUUID(authentication.getName())));
    }
}
