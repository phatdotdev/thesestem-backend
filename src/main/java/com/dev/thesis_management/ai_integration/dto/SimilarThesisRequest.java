package com.dev.thesis_management.ai_integration.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class SimilarThesisRequest {
    UUID id;
    String title;
    String description;
    UUID organizationId;
}
