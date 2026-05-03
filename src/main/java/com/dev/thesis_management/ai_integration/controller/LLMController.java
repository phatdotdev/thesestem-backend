package com.dev.thesis_management.ai_integration.controller;

import com.dev.thesis_management.ai_integration.dto.AdviseSemesterRequest;
import com.dev.thesis_management.ai_integration.dto.AnalyzeFileRequest;
import com.dev.thesis_management.ai_integration.dto.SuggestThesisResponse;
import com.dev.thesis_management.ai_integration.service.AdvisoryService;
import com.dev.thesis_management.ai_integration.service.AnalyzeFileService;
import com.dev.thesis_management.ai_integration.service.SuggestionService;
import com.dev.thesis_management.common.response.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;
import static com.dev.thesis_management.common.utils.UUIDUtils.*;

@RestController
@RequestMapping("/llm")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LLMController {

    AnalyzeFileService analyzeFileService;
    SuggestionService suggestionService;
    AdvisoryService advisoryService;

    @PostMapping("/file")
    public ResponseEntity<ApiResponse<Void>> analyzeFile(
            @RequestBody AnalyzeFileRequest request
            ){
        return ok(analyzeFileService.analyzeFile(request.fileId(), request.userPrompt()));
    }

    @PostMapping("/theses-suggestion/{thesisId}")
    public ResponseEntity<ApiResponse<SuggestThesisResponse>> suggestTopics(
            @PathVariable("thesisId") UUID thesisId
    ) {
        return ok(suggestionService.suggestTopics(thesisId));
    }

    @PostMapping("/semester-advise")
    public ResponseEntity<ApiResponse<Void>> suggestThesesForSemester(
            @RequestBody AdviseSemesterRequest request,
            Authentication authentication
    ) {
        return ok(advisoryService.adviseForSemester(request.id(), request.userPrompt(), parseUUID(authentication.getName())));
    }
}
