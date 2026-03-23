package com.dev.thesis_management.thesis.mapper;

import com.dev.thesis_management.thesis.dto.thesis.ThesisResponse;
import com.dev.thesis_management.thesis.entity.Thesis;
import com.dev.thesis_management.user.mapper.LecturerMapper;
import com.dev.thesis_management.user.mapper.StudentMapper;

public class ThesisMapper {
    public static ThesisResponse toThesisResponse(Thesis thesis){
        return ThesisResponse.builder()
                .id(thesis.getId())
                .title(thesis.getTitle())
                .titleEn(thesis.getTitleEn())
                .description(thesis.getDescription())
                .descriptionEn(thesis.getDescriptionEn())
                .progressPercent(thesis.getProgressPercent())
                .student(StudentMapper.studentToResponse(thesis.getStudent()))
                .build();
    }

    public static ThesisResponse toThesisResponseWithMentor(Thesis thesis){
        return ThesisResponse.builder()
                .id(thesis.getId())
                .title(thesis.getTitle())
                .titleEn(thesis.getTitleEn())
                .description(thesis.getDescription())
                .descriptionEn(thesis.getDescriptionEn())
                .progressPercent(thesis.getProgressPercent())
                .student(StudentMapper.studentToResponse(thesis.getStudent()))
                .mentor(LecturerMapper.lecturerToResponse(thesis.getTopic().getGroup().getMentor()))
                .build();
    }
}
