package com.dev.thesis_management.ai_integration.service;

import com.dev.thesis_management.exception.BadRequestException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GeminiService {

    WebClient webClient;

    @NonFinal
    @Value("${gemini.api.key}")
    String apiKey;

    @NonFinal
    @Value("${gemini.api.url}")
    String apiUrl;

    @NonFinal
    @Value("${gemini.api.max-output-tokens:1024}")
    int maxOutputTokens;

    @NonFinal
    @Value("${gemini.api.max-prompt-chars:6000}")
    int maxPromptChars;

    @NonFinal
    @Value("${gemini.api.max-inline-file-bytes:3500000}")
    int maxInlineFileBytes;

    @NonFinal
    @Value("${gemini.api.max-inline-base64-chars:4800000}")
    int maxInlineBase64Chars;

    @NonFinal
    @Value("${gemini.api.retry-rounds:0}")
    int retryRounds;

    @NonFinal
    @Value("${gemini.api.retry-delay-ms:500}")
    long retryDelayMs;

    @NonFinal
    @Value("${gemini.api.enforce-limits:false}")
    boolean enforceLimits;

    @NonFinal
    @Value("${gemini.api.advisory-text-file-threshold-chars:9000}")
    int advisoryTextFileThresholdChars;

    public String analyzeFile(byte[] fileBytes, String mimeType, String userPrompt) {
        validateFileSize(fileBytes);
        String safePrompt = normalizePrompt(userPrompt);

        String base64 = Base64.getEncoder().encodeToString(fileBytes);
        if (enforceLimits && base64.length() > maxInlineBase64Chars) {
            throw new BadRequestException("File quá lớn để AI phân tích. Vui lòng giảm dung lượng hoặc tách file nhỏ hơn.");
        }

        Map<String, Object> request = new HashMap<>();
        request.put("contents", List.of(
                Map.of("parts", List.of(
                        Map.of("text", """
                                Bạn là chuyên gia đánh giá luận văn.

                                Hãy trả lời dựa trên nội dung file.

                                Yêu cầu:

                                %s
                                """.formatted(safePrompt)),
                        Map.of("inlineData", Map.of(
                                "mimeType", mimeType,
                                "data", base64
                        ))
                ))
        ));

        if (enforceLimits && maxOutputTokens > 0) {
            request.put("generationConfig", Map.of("maxOutputTokens", maxOutputTokens));
        }

        return extractText(postWithRetry(request));
    }

    public String callGeminiText(String prompt) {
        String safePrompt = normalizePrompt(prompt);

        Map<String, Object> request = new HashMap<>();
        request.put("contents", List.of(
                Map.of("parts", List.of(Map.of("text", safePrompt)))
        ));

        if (enforceLimits && maxOutputTokens > 0) {
            request.put("generationConfig", Map.of("maxOutputTokens", maxOutputTokens));
        }

        return extractText(postWithRetry(request));
    }

    public String callGeminiTextForAdvisory(String prompt) {
        String safePrompt = normalizePrompt(prompt);

        Map<String, Object> request = shouldSendAdvisoryAsTextFile(safePrompt)
                ? buildAdvisoryTextFileRequest(safePrompt)
                : buildAdvisoryInlineTextRequest(safePrompt);

        if (enforceLimits && maxOutputTokens > 0) {
            request.put("generationConfig", Map.of("maxOutputTokens", maxOutputTokens));
        }

        return extractText(postWithRetry(request));
    }

    private Map<String, Object> buildAdvisoryInlineTextRequest(String safePrompt) {
        Map<String, Object> request = new HashMap<>();
        request.put("contents", List.of(
                Map.of("parts", List.of(Map.of("text", safePrompt)))
        ));
        return request;
    }

    private Map<String, Object> buildAdvisoryTextFileRequest(String safePrompt) {
        String textFileBase64 = Base64.getEncoder()
                .encodeToString(safePrompt.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> request = new HashMap<>();
        request.put("contents", List.of(
                Map.of("parts", List.of(
                        Map.of("text", "Hãy trả lời dựa trên file văn bản đính kèm. Chỉ dùng dữ liệu trong file."),
                        Map.of("inlineData", Map.of(
                                "mimeType", "text/plain",
                                "data", textFileBase64
                        ))
                ))
        ));

        log.info("Advisory prompt is large ({} chars), sending as text/plain inline file", safePrompt.length());
        return request;
    }

    private boolean shouldSendAdvisoryAsTextFile(String safePrompt) {
        if (!enforceLimits) {
            return safePrompt.length() >= advisoryTextFileThresholdChars;
        }

        return safePrompt.length() >= Math.min(maxPromptChars, advisoryTextFileThresholdChars);
    }

    private String extractText(Map response) {
        try {
            List candidates = (List) response.get("candidates");
            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");
            Map textPart = (Map) parts.get(0);
            return textPart.get("text").toString();
        } catch (Exception e) {
            return "Không đọc được phản hồi";
        }
    }

    private Map postWithRetry(Map<String, Object> request) {
        Exception lastError = null;

        for (int round = 0; round <= retryRounds; round++) {
            try {
                return webClient.post()
                        .uri(apiUrl + "?key=" + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
            } catch (WebClientResponseException ex) {
                if (!isRetryable(ex)) {
                    if (ex.getStatusCode().value() == 413) {
                        throw new BadRequestException("Nội dung gửi lên quá lớn. Vui lòng rút gọn prompt hoặc giảm kích thước file.");
                    }
                    throw new BadRequestException(
                            "Gemini từ chối yêu cầu (status: " + ex.getStatusCode().value() + "). Có thể prompt/file quá lớn hoặc sai định dạng."
                    );
                }

                lastError = ex;
                log.warn("Gemini failed with status {} (round {}/{})", ex.getStatusCode().value(), round + 1, retryRounds + 1);
            } catch (Exception ex) {
                lastError = ex;
                log.warn("Gemini network error (round {}/{}): {}", round + 1, retryRounds + 1, ex.getMessage());
            }

            if (round < retryRounds) {
                sleepBeforeRetry();
            }
        }

        if (lastError instanceof WebClientResponseException ex) {
            throw new BadRequestException("Gemini tạm thời không phản hồi (status: " + ex.getStatusCode().value() + "). Vui lòng thử lại sau.");
        }

        throw new BadRequestException("Gemini tạm thời không phản hồi. Vui lòng thử lại sau.");
    }

    private boolean isRetryable(WebClientResponseException ex) {
        int statusCode = ex.getStatusCode().value();
        return statusCode == 401 || statusCode == 403 || statusCode == 429 || statusCode >= 500;
    }

    private String normalizePrompt(String prompt) {
        if (prompt == null) {
            return "";
        }

        String cleaned = prompt.trim();
        if (!enforceLimits) {
            return cleaned;
        }

        if (cleaned.isBlank()) {
            throw new BadRequestException("Prompt không được để trống");
        }

        if (cleaned.length() <= maxPromptChars) {
            return cleaned;
        }

        log.warn("Gemini prompt too long ({} chars), truncating to {} chars", cleaned.length(), maxPromptChars);
        return cleaned.substring(0, maxPromptChars);
    }

    private void validateFileSize(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new BadRequestException("File rỗng hoặc không hợp lệ");
        }

        if (enforceLimits && fileBytes.length > maxInlineFileBytes) {
            throw new BadRequestException("File quá lớn để AI phân tích. Vui lòng giảm dung lượng hoặc tách file nhỏ hơn.");
        }
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(retryDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
