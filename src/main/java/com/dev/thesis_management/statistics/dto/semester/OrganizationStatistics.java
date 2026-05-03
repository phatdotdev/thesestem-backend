package com.dev.thesis_management.statistics.dto.semester;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizationStatistics {

    long totalStudents;
    long totalLecturers;
    long totalTheses;

    long totalGroups;
    long totalCouncils;
    long totalMentors;

    long proposalTheses;
    long inProgressTheses;
    long approvedTheses;
    long submittedTheses;
    long gradedTheses;

    long privateTheses;
    long internalTheses;
    long publicTheses;

    long totalDefenses;

    Double averageThesisScore;
}
