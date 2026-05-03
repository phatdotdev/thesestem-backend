package com.dev.thesis_management.ai_integration.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SimilarTheisListResponse {
    List<SimilarThesisResponse> suggestions;
}
