package com.dev.thesis_management.statistics.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.statistics.dto.SystemStatistics;
import com.dev.thesis_management.statistics.dto.mentor.MentorStatistics;
import com.dev.thesis_management.statistics.dto.semester.OrganizationStatistics;
import com.dev.thesis_management.statistics.dto.semester.OrganizationStatisticsRequest;
import com.dev.thesis_management.statistics.dto.semester.SemesterStatistic;
import com.dev.thesis_management.statistics.service.MentorStatisticsService;
import com.dev.thesis_management.statistics.service.StatisticsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.ok;
import static com.dev.thesis_management.common.utils.UUIDUtils.parseUUID;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticsController {

    StatisticsService statisticsService;
    MentorStatisticsService mentorStatisticsService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/system")
    public ResponseEntity<ApiResponse<SystemStatistics>> getSystemStatistics(Authentication authentication) {
        return ok(statisticsService.getSystemStatistics(parseUUID(authentication.getName())));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/semesters/{semesterId}")
    public ResponseEntity<ApiResponse<SemesterStatistic>> getSemesterStatistics(
            @PathVariable UUID semesterId,
            Authentication authentication
    ) {
        return ok(statisticsService.getSemesterStatistics(semesterId, parseUUID(authentication.getName())));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/organization")
    public ResponseEntity<ApiResponse<OrganizationStatistics>> getOrganizationStatistics(
            @ModelAttribute OrganizationStatisticsRequest request,
            Authentication authentication
    ) {
        return ok(statisticsService.getOrganizationStatistics(request, parseUUID(authentication.getName())));
    }

    @GetMapping("/mentor")
    public ResponseEntity<ApiResponse<MentorStatistics>> getMentorStatistics(Authentication authentication) {
       return ok(mentorStatisticsService.getMentorStatisticsAllSemesters(parseUUID(authentication.getName())));
    }

    @GetMapping("/mentor/semester/{semesterId}")
    public ResponseEntity<ApiResponse<MentorStatistics>> getMentorStatisticsBySemester(
                @PathVariable UUID semesterId,
            Authentication authentication) {
        return ok(mentorStatisticsService.getMentorStatisticsBySemester(parseUUID(authentication.getName()), semesterId));
    }
}
