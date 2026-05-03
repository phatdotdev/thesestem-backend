package com.dev.thesis_management.ai_integration.service;

import com.dev.thesis_management.ai_integration.dto.*;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.thesis.entity.Thesis;
import com.dev.thesis_management.thesis.repository.ThesisRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class EmbeddingService {

    ThesisRepository thesisRepository;
    WebClient webClient;

    @NonFinal
    @Value("${embedding.thesis-url}")
    String embeddingUrl;

    public void syncThesisEmbeddings() {
        List<Thesis> theses = thesisRepository.findAll();
        List<ThesisEmbeddingRequest> request = new ArrayList<>();
        for (Thesis thesis : theses) {
            ThesisEmbeddingRequest embedding = ThesisEmbeddingRequest.builder()
                    .id(thesis.getId())
                    .title(thesis.getTitle())
                    .description(thesis.getDescription())
                    .access(thesis.getAccessLevel().name())
                    .organizationId(thesis.getTopic().getGroup().getSemester().getOrganization().getId())
                    .organizationName(thesis.getTopic().getGroup().getSemester().getOrganization().getName())
                    .build();
            request.add(embedding);
        }

        webClient.post()
                .uri(embeddingUrl+ "/batch")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public List<SimilarThesisResponse> getSimilarTheses(UUID thesisId) {

        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() ->
                        new BadRequestException("Thesis not found with id: " + thesisId));

        SimilarThesisRequest request = SimilarThesisRequest.builder()
                .id(thesis.getId())
                .title(thesis.getTitle())
                .description(thesis.getDescription())
                .organizationId(
                        thesis.getTopic()
                                .getGroup()
                                .getSemester()
                                .getOrganization()
                                .getId()
                )
                .build();

        SimilarTheisListResponse response = webClient.post()
                .uri(embeddingUrl + "/suggest")
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        res -> res.bodyToMono(String.class)
                                .map(BadRequestException::new)
                )
                .onStatus(
                        status -> status.is5xxServerError(),
                        res -> res.bodyToMono(String.class)
                                .map(RuntimeException::new)
                )
                .bodyToMono(SimilarTheisListResponse.class)
                .block();

        assert response != null;

        System.out.println("Received " + response.getSuggestions());
        return response.getSuggestions();
    }
}
