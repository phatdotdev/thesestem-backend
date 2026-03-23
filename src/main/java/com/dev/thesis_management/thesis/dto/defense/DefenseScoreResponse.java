package com.dev.thesis_management.thesis.dto.defense;

import com.dev.thesis_management.thesis.dto.council.CouncilMemberResponse;
import com.dev.thesis_management.user.dto.LecturerResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DefenseScoreResponse {
    UUID id;
    CouncilMemberResponse member;
    Double score;
    String comment;
}
