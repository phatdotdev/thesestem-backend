package com.dev.thesis_management.thesis.mapper;

import com.dev.thesis_management.thesis.dto.organization.OrganizationResponse;
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
                .status(thesis.getStatus())
                .accessLevel(thesis.getAccessLevel())
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
                .status(thesis.getStatus())
                .accessLevel(thesis.getAccessLevel())
                .student(StudentMapper.studentToResponse(thesis.getStudent()))
                .mentor(LecturerMapper.lecturerToResponse(thesis.getTopic().getGroup().getMentor()))
                .build();
    }

    public static ThesisResponse toThesisResponseWithMentorAndOrganization(Thesis thesis){
        return ThesisResponse.builder()
                .id(thesis.getId())
                .title(thesis.getTitle())
                .titleEn(thesis.getTitleEn())
                .description(thesis.getDescription())
                .descriptionEn(thesis.getDescriptionEn())
                .progressPercent(thesis.getProgressPercent())
                .status(thesis.getStatus())
                .accessLevel(thesis.getAccessLevel())
                .student(StudentMapper.studentToResponse(thesis.getStudent()))
                .mentor(LecturerMapper.lecturerToResponse(thesis.getTopic().getGroup().getMentor()))
                .organization(OrganizationResponse.builder()
                        .id(thesis.getTopic().getGroup().getSemester().getOrganization().getId())
                        .name(thesis.getTopic().getGroup().getSemester().getOrganization().getName())
                        .build())
                .build();
    }

    public static ThesisResponse toThesisResponseWithMentorAndOrganizationAndSubmissions(Thesis thesis) {
        return ThesisResponse.builder()
                .id(thesis.getId())
                .title(thesis.getTitle())
                .titleEn(thesis.getTitleEn())
                .description(thesis.getDescription())
                .descriptionEn(thesis.getDescriptionEn())
                .progressPercent(thesis.getProgressPercent())
                .status(thesis.getStatus())
                .accessLevel(thesis.getAccessLevel())
                .student(StudentMapper.studentToResponse(thesis.getStudent()))
                .mentor(LecturerMapper.lecturerToResponse(thesis.getTopic().getGroup().getMentor()))
                .organization(OrganizationResponse.builder()
                        .id(thesis.getTopic().getGroup().getSemester().getOrganization().getId())
                        .name(thesis.getTopic().getGroup().getSemester().getOrganization().getName())
                        .build())
                .submissions(thesis.getSubmissions() != null ? thesis.getSubmissions().stream()
                        .map(SubmissionMapper::toResponse)
                        .toList() : null)
                .build();
    }
}
