package com.dev.thesis_management.thesis.dto.council;

import java.util.List;

public record CouncilRequest(
        String name,
        String code,
        List<CouncilMemberRequest> members
) {}
