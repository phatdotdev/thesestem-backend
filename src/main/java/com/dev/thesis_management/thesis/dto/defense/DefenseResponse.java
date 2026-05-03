package com.dev.thesis_management.thesis.dto.defense;

import com.dev.thesis_management.file_asset.dto.FileAssetResponse;
import com.dev.thesis_management.thesis.dto.council.CouncilMemberResponse;
import com.dev.thesis_management.thesis.dto.council.CouncilResponse;
import com.dev.thesis_management.thesis.dto.thesis.ThesisResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefenseResponse {
    UUID id;
    LocalDateTime defenseTime;
    String location;
    ThesisResponse thesis;
    CouncilResponse council;
    List<DefenseScoreResponse> scores;
    FileAssetResponse minutesFile;
    CouncilMemberResponse member;
}
