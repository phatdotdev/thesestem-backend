package com.dev.thesis_management.organization.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.organization.dto.AddProgramRequest;
import com.dev.thesis_management.organization.dto.ProgramResponse;
import com.dev.thesis_management.organization.dto.UpdateProgramRequest;
import com.dev.thesis_management.organization.dto.organization.ProgramSearchForm;
import com.dev.thesis_management.organization.service.ProgramService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.common.utils.UUIDUtils.*;
import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;

@RestController
@RequestMapping("/programs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProgramController {
    ProgramService programService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProgramResponse>>> getPrograms(
            @ModelAttribute ProgramSearchForm form,
            Authentication authentication
    ){
        return ok(programService.getPrograms(form, parseUUID(authentication.getName())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProgramResponse>> createProgram(
            Authentication authentication,
            @RequestBody AddProgramRequest request
            ){
        return created(programService.addProgram(request, parseUUID(authentication.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProgramResponse>> updateProgram(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody UpdateProgramRequest request
    ){
        return ok(programService.updateProgram(request, id, parseUUID(authentication.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProgram(
            Authentication authentication,
            @PathVariable UUID id
    ){
        programService.deleteProgram(id, parseUUID(authentication.getName()));
        return noContent();
    }
}
