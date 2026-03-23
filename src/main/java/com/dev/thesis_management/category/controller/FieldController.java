package com.dev.thesis_management.category.controller;

import com.dev.thesis_management.category.entity.Course;
import com.dev.thesis_management.category.entity.Field;
import com.dev.thesis_management.category.service.FieldService;
import com.dev.thesis_management.common.response.ApiResponse;
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
@RequestMapping("/fields")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FieldController {
    FieldService fieldService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Field>>> getFields(Authentication authentication){
        return ok(fieldService.getFields(parseUUID(authentication.getName())));
    }
    @PostMapping
    public ResponseEntity<ApiResponse<Field>> createField(
            Authentication authentication,
            @RequestBody Field request
    ){
        return created(fieldService.createField(request, parseUUID(authentication.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Field>> updateField(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody Field request
    ){
        return ok(fieldService.updateField(id, request, parseUUID(authentication.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteField(
            Authentication authentication,
            @PathVariable UUID id
    ){
        fieldService.deleteField(id, parseUUID(authentication.getName()));
        return noContent();
    }
}
