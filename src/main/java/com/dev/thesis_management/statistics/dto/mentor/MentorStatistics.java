package com.dev.thesis_management.statistics.dto.mentor;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MentorStatistics {
    Long totalRegisteredStudents;
    Long totalAcceptedRegistrations;
    Long totalRejectedRegistrations;
    Long totalCancelledRegistrations;
    Long totalTheses;
    Long totalGroups;
    Long totalCouncils;
    Double averageThesisScore;
    Double averageGivenScore;
}
