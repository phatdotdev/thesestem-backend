package com.dev.thesis_management.thesis.mapper;

import com.dev.thesis_management.file_asset.mapper.FileMapper;
import com.dev.thesis_management.thesis.dto.defense.DefenseResponse;
import com.dev.thesis_management.thesis.dto.defense.DefenseScoreResponse;
import com.dev.thesis_management.thesis.entity.DefenseScore;
import com.dev.thesis_management.thesis.entity.ThesisDefense;

import java.util.ArrayList;

public class DefenseMapper {
    public static DefenseResponse toDefenseResponse(ThesisDefense defense){
        return DefenseResponse.builder()
                .id(defense.getId())
                .council(CouncilMapper.toCouncilResponse(defense.getCouncil()))
                .thesis(ThesisMapper.toThesisResponseWithMentor(defense.getThesis()))
                .scores(
                        defense.getScores() != null
                                ? new ArrayList<>(defense.getScores())
                                .stream()
                                .map(DefenseMapper::toDefenseScoreResponse)
                                .toList()
                                : null
                )
                .defenseTime(defense.getDefenseTime())
                .location(defense.getLocation())
                .minutesFile(FileMapper.toFileResponse(defense.getMinutesFile()))
                .build();
    }

    public static DefenseScoreResponse toDefenseScoreResponse(DefenseScore score){
        return DefenseScoreResponse.builder()
                .id(score.getId())
                .score(score.getScore())
                .comment(score.getComment())
                .member(CouncilMapper.toMemberResponse(score.getCouncilMember()))
                .build();
    }
}
