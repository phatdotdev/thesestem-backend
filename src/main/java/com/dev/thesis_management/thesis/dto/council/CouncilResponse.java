package com.dev.thesis_management.thesis.dto.council;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouncilResponse {
    UUID id;
    String name;
    String code;
    List<CouncilMemberResponse> members;
}
