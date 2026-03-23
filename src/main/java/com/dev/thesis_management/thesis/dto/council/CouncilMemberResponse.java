package com.dev.thesis_management.thesis.dto.council;

import com.dev.thesis_management.thesis.entity.CouncilRole;
import com.dev.thesis_management.user.dto.LecturerResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouncilMemberResponse {
    UUID id;
   LecturerResponse lecturer;
   CouncilRole role;
    boolean isCurrentUser;
}
