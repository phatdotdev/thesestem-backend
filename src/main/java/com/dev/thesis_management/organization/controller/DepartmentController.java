package com.dev.thesis_management.organization.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.organization.dto.AddDepartmentRequest;
import com.dev.thesis_management.organization.dto.DepartmentResponse;
import com.dev.thesis_management.organization.dto.UpdateDepartmentRequest;
import com.dev.thesis_management.organization.service.DepartmentService;
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
@RequiredArgsConstructor
@RequestMapping("/departments")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentController {

    DepartmentService departmentService;

    @GetMapping
    ResponseEntity<ApiResponse<List<DepartmentResponse>>> getDepartments(
            Authentication authentication
    ){
        return ok(departmentService.getDepartments(parseUUID(authentication.getName())));
    }

    @PostMapping
    ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @RequestBody AddDepartmentRequest request,
            Authentication authentication
    ){
        return created(departmentService.addDepartment(request, parseUUID(authentication.getName())));
    }

    @PutMapping("/{departmentId}")
    ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            Authentication authentication,
            @PathVariable UUID departmentId,
            @RequestBody UpdateDepartmentRequest request
            ){
        return ok(departmentService.updateDepartment(departmentId, request, parseUUID(authentication.getName())));
    }

    @DeleteMapping("/{departmentId}")
    ResponseEntity<ApiResponse> updateDepartment(
            Authentication authentication,
            @PathVariable UUID departmentId
    ){
        departmentService.deleteDepartment(departmentId, parseUUID(authentication.getName()));
        return noContent();
    }
}
