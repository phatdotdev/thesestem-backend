package com.dev.thesis_management.category.service;

import com.dev.thesis_management.category.entity.Course;
import com.dev.thesis_management.category.repository.CourseRepository;
import com.dev.thesis_management.exception.UnauthorizedException;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.service.OrgService;
import com.dev.thesis_management.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseService {
    UserRepository userRepository;
    CourseRepository courseRepository;
    OrgService orgService;

    @Transactional(readOnly = true)
    public List<Course> getAllCourses(UUID userId) {
        Organization org = orgService.findByUserId(userId);

        return courseRepository.findAllByOrganizationId(org.getId());
    }


    public Course addCourse(Course request, UUID userId){
        Organization org = userRepository.findById(userId).orElseThrow().getOrganization();
        if(!org.getManager().getId().equals(userId)){
            throw new UnauthorizedException("");
        }
        return courseRepository.save(Course.builder()
                .name(request.getName())
                .code(request.getCode())
                .startYear(request.getStartYear())
                .endYear(request.getEndYear())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(true)
                .organization(org)
                .build());
    }

    @Transactional
    public Course updateCourse(UUID courseId, Course request, UUID userId) {
        Organization org = userRepository.findById(userId)
                .orElseThrow()
                .getOrganization();

        if (!org.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getOrganization().getId().equals(org.getId())) {
            throw new UnauthorizedException("");
        }

        course.setName(request.getName());
        course.setCode(request.getCode());
        course.setStartYear(request.getStartYear());
        course.setEndYear(request.getEndYear());
        course.setStartDate(request.getStartDate());
        course.setEndDate(request.getEndDate());
        course.setActive(request.getActive());

        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(UUID courseId, UUID userId) {
        Organization org = userRepository.findById(userId)
                .orElseThrow()
                .getOrganization();

        if (!org.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getOrganization().getId().equals(org.getId())) {
            throw new UnauthorizedException("");
        }

//        if (!course.getStudents().isEmpty()) {
//            throw new RuntimeException("Course is in use, cannot delete");
//        }

        courseRepository.delete(course);
    }

}
