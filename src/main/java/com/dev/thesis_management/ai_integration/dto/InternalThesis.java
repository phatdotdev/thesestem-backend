package com.dev.thesis_management.ai_integration.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class InternalThesis {
    UUID id;
    String title;
    String description;
    String organizationName;
    Double score;
    String reason;
}
