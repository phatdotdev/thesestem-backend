package com.dev.thesis_management.ai_integration.controller;

import com.dev.thesis_management.ai_integration.service.EmbeddingService;
import com.dev.thesis_management.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;

@RestController
@RequestMapping("/embeddings")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class EmbeddingController {

    EmbeddingService embeddingService;

    @PostMapping("/theses/sync")
    public ResponseEntity<ApiResponse> generateEmbeddings() {
        embeddingService.syncThesisEmbeddings();
        return noContent();
    }

}
