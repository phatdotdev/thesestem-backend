package com.dev.thesis_management.thesis.mapper;

import com.dev.thesis_management.thesis.dto.RegisterResponse;
import com.dev.thesis_management.thesis.entity.MentorRegister;

import static com.dev.thesis_management.user.mapper.LecturerMapper.*;
import static com.dev.thesis_management.user.mapper.StudentMapper.*;

public class RegisterMapper {
    public static RegisterResponse toRegisterResponse(MentorRegister register){
        return RegisterResponse.builder()
                .id(register.getId())
                .student(studentToResponse(register.getStudent()))
                .mentor(lecturerToResponse(register.getMentor()))
                .message(register.getMessage())
                .status(register.getStatus())
                .createdAt(register.getCreatedAt())
                .updatedAt(register.getUpdatedAt())
                .build();
    }
}
