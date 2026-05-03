package com.dev.thesis_management.ai_integration.service;

import com.dev.thesis_management.statistics.dto.semester.SemesterStatistic;
import com.dev.thesis_management.statistics.service.StatisticsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdvisoryService {

    StatisticsService statisticsService;
    GeminiService geminiService;
    ObjectMapper objectMapper;

    @Transactional
    public String adviseForSemester(
            UUID semesterId,
            String userPrompt,
            UUID userId
    ) {
        System.out.println("AdvisoryService.adviseForSemester called with semesterId: " + semesterId + ", userPrompt: " + userPrompt + ", userId: " + userId);

        SemesterStatistic statistics =
                statisticsService.getSemesterStatistics(semesterId, userId);

        try {
            String statisticText =
                    objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(statistics);
            String prompt = """
            Bạn là chuyên gia phân tích dữ liệu học thuật và quản lý luận văn đại học.

            Dữ liệu học kỳ:

            %s

            ------------------------

            Yêu cầu:

            - chỉ dùng dữ liệu trên
            - không tạo dữ liệu mới
            - trả lời tiếng Việt
            - phân tích rõ ràng
            - tối đa 200 từ

            ------------------------

            Câu hỏi:

            %s
            """.formatted(
                    statisticText,
                    userPrompt
            );

            log.info("Semester advisory prompt:\n{}", prompt);

            return geminiService.callGeminiTextForAdvisory(prompt);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert statistics to text", e);
        }
    }

}
