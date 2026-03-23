package com.dev.thesis_management.organization.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.organization.dto.AddCollegeRequest;
import com.dev.thesis_management.organization.dto.AddFacultyRequest;
import com.dev.thesis_management.organization.dto.CollegeResponse;
import com.dev.thesis_management.organization.dto.UpdateCollegeRequest;
import com.dev.thesis_management.organization.service.CollegeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;
import static com.dev.thesis_management.common.utils.UUIDUtils.*;


@RestController
@RequestMapping("/colleges")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CollegeController {

    CollegeService collegeService;

    @GetMapping
    ResponseEntity<ApiResponse<List<CollegeResponse>>> getAllColleges(Authentication authentication){
        return ok(collegeService.getAllColleges(parseUUID(authentication.getName())));
    }

    @PostMapping
    ResponseEntity<ApiResponse<CollegeResponse>> addCollege(
            Authentication authentication,
            @RequestBody AddCollegeRequest request
            ){
        return ok(collegeService.addCollege(request, parseUUID(authentication.getName())));
    }

    @PutMapping("/{collegeId}")
    ResponseEntity<ApiResponse<CollegeResponse>> updateCollege(
            Authentication authentication,
            @PathVariable UUID collegeId,
            @RequestBody UpdateCollegeRequest request
    ){
        return ok(collegeService.updateCollege(collegeId, request, parseUUID(authentication.getName())));
    }

    @PostMapping("/{collegeId}/faculties")
    ResponseEntity<ApiResponse<CollegeResponse>> addFaculties(
            Authentication authentication,
            @PathVariable UUID collegeId,
            @RequestBody AddFacultyRequest request
    ) throws AccessDeniedException {
        return ok(collegeService.addFaculty(collegeId, request, parseUUID(authentication.getName())));
    }

    @DeleteMapping("/{collegeId}")
    ResponseEntity<ApiResponse> addFaculties(
            Authentication authentication,
            @PathVariable UUID collegeId
    ) throws AccessDeniedException {
        collegeService.deleteCollege(collegeId, parseUUID(authentication.getName()));
        return noContent();
    }
}
