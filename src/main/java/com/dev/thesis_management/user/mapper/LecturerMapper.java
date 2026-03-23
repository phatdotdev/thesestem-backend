package com.dev.thesis_management.user.mapper;

import com.dev.thesis_management.organization.mapper.CollegeMapper;
import com.dev.thesis_management.organization.mapper.DepartmentMapper;
import com.dev.thesis_management.organization.mapper.FacultyMapper;
import com.dev.thesis_management.user.dto.CreateLecturerRequest;
import com.dev.thesis_management.user.dto.LecturerResponse;
import com.dev.thesis_management.user.entity.Lecturer;

public class LecturerMapper {
    public static Lecturer createLecturerRequestToLecturer(CreateLecturerRequest request){
        return Lecturer.builder()
                .lecturerCode(request.lecturerCode())
                .fullName(request.fullName())
                .phone(request.phone())
                .dob(request.dob())
                .gender(request.gender())
                .email(request.email())
                .address(request.address())
                .build();
    }

    public static LecturerResponse lecturerToResponse(Lecturer lecturer){
        return LecturerResponse.builder()
                .id(lecturer.getId())
                .lecturerCode(lecturer.getLecturerCode())
                .fullName(lecturer.getFullName())
                .email(lecturer.getEmail())
                .gender(lecturer.getGender())
                .phone(lecturer.getPhone())
                .address(lecturer.getAddress())
                .dob(lecturer.getDob())
                .avatarUrl(lecturer.getAvatar() != null
                        ? lecturer.getAvatar().getPath()
                        : "")
                .college(lecturer.getCollege() != null ? CollegeMapper.collegeToResponse(lecturer.getCollege()) : null)
                .faculty(lecturer.getFaculty() != null ? FacultyMapper.facultyToResponse(lecturer.getFaculty()) : null)
                .department(lecturer.getDepartment() != null ? DepartmentMapper.toDepartmentResponse(lecturer.getDepartment()) : null)
                .build();
    }
}
