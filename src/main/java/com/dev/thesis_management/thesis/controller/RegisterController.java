package com.dev.thesis_management.thesis.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.thesis.dto.CreateGroupRequest;
import com.dev.thesis_management.thesis.dto.CreateRegisterRequest;
import com.dev.thesis_management.thesis.dto.RegisterResponse;
import com.dev.thesis_management.thesis.dto.register.UpdateRegisterRequest;
import com.dev.thesis_management.thesis.service.RegisterService;
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
@RequestMapping("/registers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RegisterController {

    RegisterService registerService;

    @GetMapping("/student")
    public ResponseEntity<ApiResponse<List<RegisterResponse>>> getStudentRegisters(
            Authentication authentication
    ){
        return ok(registerService.getStudentRegisters(parseUUID(authentication.getName())));
    }

    @GetMapping("/mentor")
    public ResponseEntity<ApiResponse<List<RegisterResponse>>> getMentorRegisters(
            Authentication authentication
    ){
        return ok(registerService.getMentorRegisters(parseUUID(authentication.getName())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RegisterResponse>> createRegister(
            Authentication authentication,
            @RequestBody CreateRegisterRequest request
            ){
        return created(registerService.createRegister(request, parseUUID(authentication.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RegisterResponse>> updateRegister(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody UpdateRegisterRequest request
            ){
        return ok(registerService.updateRegisterStatus(id, request, parseUUID(authentication.getName())));
    }
}
