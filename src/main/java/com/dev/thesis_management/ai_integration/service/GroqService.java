package com.dev.thesis_management.ai_integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class GroqService {

    @NonFinal
    @Value("${groq.api.key}")
    private String apiKey;

    @NonFinal
    @Value("${groq.api.url}")
    private String apiUrl;

    private final WebClient webClient;

    public String askAI(String prompt) {
        Map<String, Object> request = Map.of(
                "model", "openai/gpt-oss-120b",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3
        );

        try {
            String responseBody = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            System.out.println("Groq response: " + root.toPrettyString());

            // More robust extraction
            JsonNode choices = root.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "No response from AI";
            }

            JsonNode message = choices.get(0).get("message");
            if (message == null || !message.has("content")) {
                return "Invalid response format from AI";
            }

            String content = message.get("content").asText();

            content = content.replaceAll("^```(?:json)?\\s*", "")
                    .replaceAll("\\s*```$", "")
                    .trim();

            return content;

        } catch (WebClientResponseException e) {
        String errorBody = e.getResponseBodyAsString();
        System.err.println("Groq HTTP Error " + e.getStatusCode() + ": " + errorBody);

        try {
            JsonNode errorJson = new ObjectMapper().readTree(errorBody);
            String errorMsg = errorJson.path("error").path("message").asText("Unknown error");
            System.err.println("Groq error message: " + errorMsg);
        } catch (Exception ignored) {}

        return "AI currently unavailable: " + e.getStatusCode();
        } catch (Exception e) {
            System.err.println("Unexpected Groq error: " + e.getMessage());
            e.printStackTrace();
            return "AI currently unavailable";
        }
    }
}
