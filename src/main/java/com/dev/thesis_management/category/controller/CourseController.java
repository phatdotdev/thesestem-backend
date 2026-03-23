package com.dev.thesis_management.category.controller;

import com.dev.thesis_management.category.entity.Course;
import com.dev.thesis_management.category.service.CourseService;
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
@RequestMapping("/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseController {

    CourseService courseService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Course>>> getCourse(Authentication authentication){
        return ok(courseService.getAllCourses(parseUUID(authentication.getName())));
    }
    @PostMapping
    public ResponseEntity<ApiResponse<Course>> createCourse(
            Authentication authentication,
            @RequestBody Course request
    ){
        return created(courseService.addCourse(request, parseUUID(authentication.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> updateCourse(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody Course request
    ){
        return ok(courseService.updateCourse(id, request, parseUUID(authentication.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCourse(
            Authentication authentication,
            @PathVariable UUID id
    ){
        courseService.deleteCourse(id, parseUUID(authentication.getName()));
        return noContent();
    }
}
