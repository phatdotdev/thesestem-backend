package com.dev.thesis_management.organization.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.organization.dto.AddDepartmentRequest;
import com.dev.thesis_management.organization.dto.AddFacultyRequest;
import com.dev.thesis_management.organization.dto.FacultyResponse;
import com.dev.thesis_management.organization.dto.UpdateFacultyRequest;
import com.dev.thesis_management.organization.service.FacultyService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;
import static com.dev.thesis_management.common.utils.UUIDUtils.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/faculties")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class FacultyController {

    FacultyService facultyService;

    @GetMapping
    ResponseEntity<ApiResponse<List<FacultyResponse>>> getFaculties(
            Authentication authentication
    ){
        return ok(facultyService.getFaculties(parseUUID(authentication.getName())));
    }

    @PostMapping
    ResponseEntity<ApiResponse<FacultyResponse>> addFaculty(
            Authentication authentication,
            @RequestBody AddFacultyRequest request
    ) throws BadRequestException {
        return ok(facultyService.addFaculty(request, parseUUID(authentication.getName())));
    }

    @PutMapping("/{facultyId}")
    ResponseEntity<ApiResponse<FacultyResponse>> updateFaculty(
        Authentication authentication,
        @PathVariable UUID facultyId,
        @RequestBody  UpdateFacultyRequest request
    ){
        return ok(facultyService.updateFaculty(facultyId, request, parseUUID(authentication.getName())));
    }

    @PostMapping("/{facultyId}/departments")
    ResponseEntity<ApiResponse<FacultyResponse>> addDepartment(
            Authentication authentication,
            @PathVariable UUID facultyId,
            @RequestBody AddDepartmentRequest request
    ){
        return ok(facultyService.addDepartment(facultyId, request, parseUUID(authentication.getName())));
    }

    @DeleteMapping("/{facultyId}")
    ResponseEntity<ApiResponse> deleteFaculty(
            Authentication authentication,
            @PathVariable UUID facultyId
    ){
        facultyService.deleteFaculty(facultyId, parseUUID(authentication.getName()));
        return noContent();
    }

}
