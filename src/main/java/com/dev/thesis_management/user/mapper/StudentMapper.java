package com.dev.thesis_management.user.mapper;

import com.dev.thesis_management.user.dto.CreateStudentRequest;
import com.dev.thesis_management.user.dto.StudentResponse;
import com.dev.thesis_management.user.entity.Student;

import static com.dev.thesis_management.organization.mapper.ProgramMapper.*;

public class StudentMapper {
    public static Student createStudentRequestToStudent(CreateStudentRequest request){
        return Student.builder()
                .studentCode(request.studentCode())
                .dob(request.dob())
                .gender(request.gender())
                .email(request.email())
                .phone(request.phone())
                .fullName(request.fullName())
                .address(request.address())
                .build();
    }

    public static StudentResponse studentToResponse(Student student){
        return StudentResponse.builder()
                .id(student.getId())
                .studentCode(student.getStudentCode())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .dob(student.getDob())
                .address(student.getAddress())
                .program(programToResponse(student.getProgram()))
                .course(student.getCourse())
                .gender(student.getGender())
                .avatarUrl(student.getAvatar() != null
                        ? student.getAvatar().getPath()
                        : ""
                )
                .build();
    }
}
