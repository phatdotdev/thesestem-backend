package com.dev.thesis_management.ai_integration.service;

import com.dev.thesis_management.ai_integration.dto.InternalThesis;
import com.dev.thesis_management.ai_integration.dto.SimilarThesisResponse;
import com.dev.thesis_management.ai_integration.dto.SuggestThesisResponse;
import com.dev.thesis_management.thesis.entity.Thesis;
import com.dev.thesis_management.thesis.repository.ThesisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SuggestionService {

    ObjectMapper objectMapper;
    EmbeddingService embeddingService;
    ThesisRepository thesisRepository;

    GeminiService geminiService;
    GroqService groqService;

    // TOPIC SUGGESTION

    public SuggestThesisResponse suggestTopics(UUID thesisId) {

        Thesis thesis = thesisRepository.findById(thesisId)
                .orElseThrow(() ->
                        new RuntimeException("Thesis not found with id: " + thesisId));

        List<SimilarThesisResponse> ragResults =
                embeddingService.getSimilarTheses(thesisId);

        if (ragResults.isEmpty()) {
            return SuggestThesisResponse.builder()
                    .internals(new ArrayList<>())
                    .externals(new ArrayList<>())
                    .build();
        }

        String topicList = ragResults.stream()
                .limit(5)
                .map(t -> """
                - id: %s
                  title: %s
                  description: %s
                  organizationName: %s
                  score: %.2f
                """.formatted(
                        t.getId(),
                        t.getTitle(),
                        t.getDescription(),
                        t.getOrganizationName(),
                        t.getScore()
                ))
                .collect(Collectors.joining("\n"));

        String prompt = """
                Bạn là chuyên gia hướng dẫn luận văn.
                 Đề tài hiện tại:
                 Title: %s
                 Description: %s
                 ---------------------
                 Danh sách đề tài trong hệ thống:
                 %s
                 ---------------------
                 Hãy đề xuất:
                 1. 5 - 10 đề tài trong hệ thống phù hợp nhất
                 2. 5 - 10 đề tài ngoài hệ thống (từ internet)
                 Yêu cầu đối với đề tài ngoài hệ thống:
                 - phải là đề tài có thật trên internet
                 - link phải là link bài báo hoặc bài nghiên cứu thật
                 - ưu tiên các nguồn:
                   https://ieeexplore.ieee.org
                   https://dl.acm.org
                   https://link.springer.com
                   https://www.sciencedirect.com
                   https://arxiv.org
                 - không được tạo link giả
                 - không được dùng link search google
                 - link phải mở được trực tiếp bài báo
                 ---------------------
                 Trả JSON:
                 {
                   "internals": [
                     {
                       "id": "",
                       "title": "",
                       "description": "",
                       "organizationName": "",
                       "score": 0.0,
                       "reason": ""
                     }
                   ],
                   "externals": [
                     {
                       "title": "",
                       "description": "",
                       "link": "",
                       "reason": ""
                     }
                   ]
                 }
                 Chỉ trả JSON
                 Không markdown
                 Không text ngoài JSON
        """.formatted(
                thesis.getTitle(),
                thesis.getDescription(),
                topicList
        );

        try {

//            String response = geminiService.callGeminiText(prompt);
            String response = groqService.askAI(prompt);

//            System.out.println("Gemini response: " + response);
            System.out.println("Groq response: " + response);

            return objectMapper.readValue(
                    response,
                    SuggestThesisResponse.class
            );

        } catch (Exception e) {

            System.out.println(e);
            System.out.println("Gemini lỗi, fallback RAG");

            List<InternalThesis> internals = ragResults.stream()
                    .map(r -> {
                        InternalThesis t = new InternalThesis();
                        t.setId(r.getId());
                        t.setTitle(r.getTitle());
                        t.setDescription(r.getDescription());
                        t.setOrganizationName(r.getOrganizationName());
                        t.setScore(r.getScore());
                        return t;
                    })
                    .toList();

            return SuggestThesisResponse.builder()
                    .internals(internals)
                    .externals(new ArrayList<>())
                    .build();
        }
    }
}
