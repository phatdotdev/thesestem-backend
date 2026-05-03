package com.dev.thesis_management.ai_integration.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ThesisEmbeddingRequest {
    UUID id;
    String title;
    String description;
    String access;
    UUID organizationId;
    String organizationName;
}
