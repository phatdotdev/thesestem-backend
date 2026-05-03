package com.dev.thesis_management.ai_integration.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestThesisResponse {
    List<InternalThesis> internals;
    List<ExternalThesis> externals;
}